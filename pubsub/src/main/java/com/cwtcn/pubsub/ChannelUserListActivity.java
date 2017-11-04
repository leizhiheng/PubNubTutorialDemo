package com.cwtcn.pubsub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.cwtcn.pubsub.Bean.User;
import com.cwtcn.pubsub.adapter.ChannelUserListAdapter;
import com.cwtcn.pubsub.interfaces.IPNPrecense;
import com.cwtcn.pubsub.service.PNService;
import com.cwtcn.pubsub.util.PLog;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 页面功能：
 * 1、显示订阅了当前channel的所有用户的用户名、用户id、用户状态
 */
public class ChannelUserListActivity extends AppCompatActivity implements IPNPrecense, ChannelUserListAdapter.OnGrantClickedlistener{
    public static final String TAG = ChannelUserListActivity.class.getSimpleName();
    private String mChannelName;
    private RecyclerView mRecyclerView;
    private ChannelUserListAdapter mAdapter;

    private PNService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PNService.PNBinder binder = (PNService.PNBinder) service;
            mService = binder.getPNService();
            mService.addPreceseCallback(ChannelUserListActivity.this);
            hereNow();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 启动ChannelUserListActivity
     * @param context
     * @param channelName
     */
    public static void start(Context context, String channelName) {
        Intent intent = new Intent(context, ChannelUserListActivity.class);
        intent.putExtra("channel", channelName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_user_list);

        init();
        startService();
    }

    private void startService() {
        Intent intent = new Intent(this, PNService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void init() {
        mChannelName = getIntent().getStringExtra("channel");

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, 10);
            }
        });
        mAdapter = new ChannelUserListAdapter(this);
        mAdapter.setOnGrantClickedlistener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 获取订阅了当前Channel的用户列表
     */
    private void hereNow() {
        mService.getPubNub().hereNow().channels(Arrays.asList(mChannelName)).includeState(true).includeUUIDs(true).async(new PNCallback<PNHereNowResult>() {
            @Override
            public void onResponse(PNHereNowResult result, PNStatus status) {
                if (status.isError()) {
                    Toast.makeText(ChannelUserListActivity.this, "HereNow operate failed!", Toast.LENGTH_LONG).show();
                    return;
                }
                final List<User> users = new ArrayList<User>();
                for (PNHereNowChannelData channelData : result.getChannels().values()) {
                    PLog.d(TAG, "herenow channel:" + channelData.getChannelName());
                    for (PNHereNowOccupantData occupantData : channelData.getOccupants()) {
                        //根据返回的数据，这里返回的用户信息，包括uuid和state.而且state的值是null。
                        //所以，如果我们想要在这里获取更多的用户信息，是不是需要每个用户在登录的时候提交state，将更多的
                        //用户信息提交上来，比如果说用户名，性别，当前状态等。
                        User user = new User(occupantData.getUuid(), occupantData.getUuid(), true, true);
                        HashMap<String, String> stateMap = (HashMap<String, String>) occupantData.getState();
                        if (stateMap.containsKey(Contants.STATE_KEY_NAME)) {
                            user.mUserName = stateMap.get(Contants.STATE_KEY_NAME);
                        }
                        if (stateMap.containsKey(Contants.STATE_KEY_STATE)) {
                            user.mState = stateMap.get(Contants.STATE_KEY_STATE);
                        }
                        users.add(user);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.refreshUsers(users);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void presence(PubNub pubnub, PNPresenceEventResult presence) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.removePrecenseCallback(this);
    }

    /**
     *
     * @param user
     */
    @Override
    public void grantRead(final User user) {
        //授权的时候需要Auth key，但是返回的用户信息是没有的。所以这个Auth key也许要通过提交state添加到用户信息里面
        //不过由于我们设置的Auth key就是uuid，所以我们这里可以传入uuid测试一下。
        mService.getPubNub().grant().channels(Arrays.asList(mChannelName)).authKeys(Arrays.asList(user.mUserId))
                .read(!user.mIsGrantedRead).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {
                    Toast.makeText(ChannelUserListActivity.this, "grantRead operate failed!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //根据result中的结果看，授权或者收回权限的请求执行成功了。但是这个用户依然可以在这个通道中接收和发布消息
                //怀疑是因为这个用户设置了SecretKey，所以这个用户拥有最高权限。授权和权限回收其实对这个用户并没有影响。
                user.mIsGrantedRead = !user.mIsGrantedRead;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void grantWrite(final User user) {
        //授权的时候需要Auth key，但是返回的用户信息是没有的。所以这个Auth key也许要通过提交state添加到用户信息里面
        PubNubApplication.getAdminPubnub().grant().channels(Arrays.asList(mChannelName)).authKeys(Arrays.asList(user.mUserId))
                .write(!user.mIsGrantedRead).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {
                    Toast.makeText(ChannelUserListActivity.this, "grantRead operate failed!", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.mIsGrantedWrite = !user.mIsGrantedWrite;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}
