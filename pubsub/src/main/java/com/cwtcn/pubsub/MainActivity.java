package com.cwtcn.pubsub;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.cwtcn.pubsub.Bean.Channel;
import com.cwtcn.pubsub.adapter.ViewPagerAdapter;
import com.cwtcn.pubsub.dao.ChannelsDao;
import com.cwtcn.pubsub.events.DBChangeEvent;
import com.cwtcn.pubsub.events.SubscribeAllEvent;
import com.cwtcn.pubsub.fragments.ChannelFragment;
import com.cwtcn.pubsub.interfaces.IPNStatus;
import com.cwtcn.pubsub.service.PNService;
import com.cwtcn.pubsub.util.GsonUtil;
import com.cwtcn.pubsub.util.PLog;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面功能
 * 1、页面包含了两个Fragment，第一个Fragment用来显示用户订阅过的Channels;第二个用户用来显示用户没有订阅过的Channels
 * 2、第一次进入页面的时候，把第一个Fragment中的channel全部订阅一遍。
 * 3、点击已订阅channel，则进入到该通道的聊天室
 * 4、点击未订阅的channel,弹出订阅提示框。点击提示框中的确定按钮则订阅通道，并跳转到通道的聊天室。并且，将修改数据库中这个通道
 *    的订阅状态
 * 5、点击菜单栏中的“+Channel”按钮，用户可以新增一个通道。这个通道将插入数据库，并显示在第二个Fragment中
 */
public class MainActivity extends AppCompatActivity implements IPNStatus{
    public static final String TAG = MainActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    SharedPreferences mPreferences;

    public Channel mCurrentChannel;

    private PNService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PNService.PNBinder binder = (PNService.PNBinder) service;
            mService = binder.getPNService();
            mService.addStatusCallback(MainActivity.this);
            EventBus.getDefault().post(new SubscribeAllEvent());
            postPrecenseState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        toBindService();

    }

    private void initData() {
        mPreferences = getSharedPreferences(Contants.PREF_NAME, MODE_PRIVATE);
    }

    private void initView() {
        //mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mPreferences.getString(Contants.PREF_KEY_USER_NAME, "Unknown"));
        actionBar.setLogo(R.mipmap.ic_launcher);

        List<String> titles = new ArrayList<>();
        titles.add("My Channels");
        titles.add("Othor Channels");
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ChannelFragment.newInstance(true));
        fragments.add(ChannelFragment.newInstance(false));

        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), titles, fragments));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void toBindService() {
        Intent intent = new Intent(this, PNService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_channel) {
            showAddChannelDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddChannelDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add channel");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_channel, null);
        final EditText et = view.findViewById(R.id.channel_name);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addChannel(et.getText().toString(), dialog);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void addChannel(String msg, DialogInterface dialog) {
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Channel名称不能为空", Toast.LENGTH_SHORT).show();
        }
        Channel channel = new Channel();
        channel.setId((ChannelsDao.queryCount()+1));
        channel.setSubscribed(false);
        channel.setChannel_name(msg);
        ChannelsDao.insertChannel(channel);
        EventBus.getDefault().post(new DBChangeEvent());
        dialog.dismiss();
    }

    public PNService getService() {
        return mService;
    }

    /**
     * 记录当前操作的Channel.以便进行数据库操作
     * @param channel
     */
    public void setCurrentChannel(Channel channel) {
        this.mCurrentChannel = channel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PubNubApplication.releaseAdminPubnub();
        mService.removeStatusCallback(this);
        unbindService(mServiceConnection);
    }

    /**
     * 进入首页后先提交用户信息，只包括userName和state
     */
    private void postPrecenseState() {
        List<Channel> channels = ChannelsDao.queryChannels(true);
        List<String> names = new ArrayList<>();
        for (Channel channel:channels) {
            names.add(channel.getChannel_name());
        }

        //user message
        JSONObject info = new JSONObject();
        try {
            info.put(Contants.STATE_KEY_NAME, PubNubApplication.sUserName);
            info.put(Contants.STATE_KEY_STATE, "online");
            mService.getPubNub().setPresenceState().channels(names).state(GsonUtil.toMap(info)).async(new PNCallback<PNSetStateResult>() {
                @Override
                public void onResponse(PNSetStateResult result, PNStatus status) {
                    if (status.isError()) {
                        return;
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void status(PubNub pubnub, PNStatus status) {
        if (status.isError()) {
            PLog.d(TAG, "操作失败，operation:" + status.getOperation() + ", category:" + status.getCategory() + ", statusCode:" + status.getStatusCode());
            return;
        }
        Log.d(TAG, "channels: operation:" + status.getOperation());
        // let's combine unsubscribe and subscribe handling for ease of use
        switch (status.getOperation()) {
            case PNUnsubscribeOperation:
                switch (status.getCategory()) {
                    case PNAcknowledgmentCategory:
                        mCurrentChannel.setSubscribed(false);
                        ChannelsDao.updateChannel(mCurrentChannel);
                        EventBus.getDefault().post(new DBChangeEvent());
                        break;
                }

                break;
            case PNSubscribeOperation:
                // note: subscribe statuses never have traditional
                // errors, they just have categories to represent the
                // different issues or successes that occur as part of subscribe
                switch (status.getCategory()) {
                    case PNConnectedCategory:
                        // this is expected for a subscribe, this means there is no error or issue whatsoever
//                        List<Channel> list = ChannelsDao.queryChannels();
                        //订阅成功修改数据库
                        mCurrentChannel.setSubscribed(true);
                        ChannelsDao.updateChannel(mCurrentChannel);
                        //发布数据库改变事件，提醒ChannelFragment重新获取通道列表
                        EventBus.getDefault().post(new DBChangeEvent());
                        //订阅成功后跳转到聊天室界面
                        //ChatRoomActivity.start(MainActivity.this, mCurrentChannel.getChannel_name());
                    case PNReconnectedCategory:
                        // this usually occurs if subscribe temporarily fails but reconnects. This means
                        // there was an error but there is no longer any issue
                    case PNDisconnectedCategory:
                        // this is the expected category for an unsubscribe. This means there
                        // was no error in unsubscribing from everything
                    case PNUnexpectedDisconnectCategory:
                        // this is usually an issue with the internet connection, this is an error, handle appropriately
                    case PNAccessDeniedCategory:
                        // this means that PAM does allow this client to subscribe to this
                        // channel and channel group configuration. This is another explicit error
                    default:
                        // More errors can be directly specified by creating explicit cases for other
                        // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                }
        }
    }
}
