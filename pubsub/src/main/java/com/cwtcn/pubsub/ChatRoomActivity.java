package com.cwtcn.pubsub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cwtcn.pubsub.Bean.Message;
import com.cwtcn.pubsub.adapter.MessageAdapter;
import com.cwtcn.pubsub.interfaces.IPNMessage;
import com.cwtcn.pubsub.service.PNService;
import com.cwtcn.pubsub.util.DateUtil;
import com.cwtcn.pubsub.util.GsonUtil;
import com.cwtcn.pubsub.util.PLog;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.orhanobut.logger.Logger;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.presence.PNGetStateResult;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天室
 * 页面功能：
 * 1、用户可以发送消息，并显示在消息列表上
 * 2、用户可以接收其它用户发布在这个通道的消息，并显示在列表上
 * 3、下拉可以获取这个通道的历史消息，每次获取10条，并显示在列表上
 * 4、点击“Menbers”菜单，可以跳转到{@ChannelUserListAdapter}页面，查看用户列表
 * 5、点击“Unsubscribe”菜单项，可以取消对这个channel的关注，取消成功，则退出页面。并更新数据库信息。
 */
public class ChatRoomActivity extends AppCompatActivity implements IPNMessage{
    public static final String TAG = ChatRoomActivity.class.getSimpleName();
    private final int REFRESH_MESSAGE_COUNT = 10;
    private String mCurrentChannel;
    private String mUserName;

    private ActionBar mActionbar;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private EditText mInput;
    private Button mSend;

    private MessageAdapter mAdapter;

    private PNService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PNService.PNBinder binder = (PNService.PNBinder) service;
            mService = binder.getPNService();
            mService.addMessageCallback(ChatRoomActivity.this);
            mService.subscribe(Arrays.asList(mCurrentChannel));
            getHistory(1, REFRESH_MESSAGE_COUNT);
            getState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 跳转到聊天室页面，传入channel
     * @param context
     * @param channelName
     */
    public static void start(Context context, String channelName) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        intent.putExtra("channel", channelName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        mCurrentChannel = getIntent().getStringExtra("channel");

        Logger.i(TAG);
        initView();
        getUserName();
        startService();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        mToolbar.setVisibility(View.GONE);
        mToolbar.setLogo(R.mipmap.ic_launcher);
        mToolbar.setTitle(mCurrentChannel);

        mActionbar = getSupportActionBar();
        mActionbar.setTitle(PubNubApplication.sUserName);
        mActionbar.setSubtitle("状态：未知");

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉加载新的历史消息
                getHistory(mAdapter.getItemCount(), REFRESH_MESSAGE_COUNT);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 0, 0, 20);
            }
        });
        mAdapter = new MessageAdapter(this);
        mRecyclerView.setAdapter(mAdapter);


        mInput = (EditText) findViewById(R.id.input);
        mSend = (Button) findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }

    private void startService() {
        Intent intent = new Intent(this, PNService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void getUserName() {
        SharedPreferences preferences = getSharedPreferences(Contants.PREF_NAME, MODE_PRIVATE);
        mUserName = preferences.getString(Contants.PREF_KEY_USER_NAME, "Unknown");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private int grantTimes;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menbers:
                //跳转到Channel用户列表
                ChannelUserListActivity.start(this, mCurrentChannel);
                break;
            case R.id.unsubscribe:
                //取消对这个Channel的订阅
                unsubscribe();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 对当前用户进行授权
     */
    private void grant() {
        boolean read = true;
        boolean write = true;

        SharedPreferences preferences = getSharedPreferences(Contants.PREF_NAME, MODE_PRIVATE);
        String uuid = preferences.getString(Contants.PREF_KEY_UUID, "");
        PubNubApplication.getAdminPubnub().grant().read(read).write(write).channels(Arrays.asList(mCurrentChannel)).authKeys(Arrays.asList(uuid)).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {

                }
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void unsubscribe() {
        mService.getPubNub().unsubscribe().channels(Arrays.asList(mCurrentChannel)).execute();
        this.finish();
    }

    /**
     * 获取用户状态
     */
    private void getState() {
        mService.getPubNub().getPresenceState().channels(Arrays.asList(mCurrentChannel)).uuid(PubNubApplication.sUuid).async(new PNCallback<PNGetStateResult>() {
            @Override
            public void onResponse(PNGetStateResult result, PNStatus status) {
                if (status.isError()) {
                    return;
                }
                Map<String, Object> channels =  result.getStateByUUID();
                for (Object obj:channels.values()) {
                    HashMap<String, String> msgs = (HashMap<String, String>) obj;
                    if (msgs.keySet().contains(Contants.STATE_KEY_STATE)) {
                        final String state = msgs.get(Contants.STATE_KEY_STATE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActionbar.setSubtitle("自定义状态：" + state);
                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    /**
     * 获取历史消息
     * @return
     */
    private void getHistory(final long start, int count) {
        mService.getPubNub().history().channel(mCurrentChannel).count(10).async(new PNCallback<PNHistoryResult>() {
            @Override
            public void onResponse(PNHistoryResult result, PNStatus status) {
                if (status.isError()) {
                    Toast.makeText(ChatRoomActivity.this, "get history operate failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                final List<Message> messages = new ArrayList<>();
                List<PNHistoryItemResult> results = result.getMessages();
                for (int i = 0; i < results.size(); i++) {
                    try {
                        Message message = GsonUtil.parserJsonToArrayBean(results.get(i).getEntry().toString(), Message.class);
                        message.isMe = PubNubApplication.sUserName.equals(message.user_name);
                        messages.add(message);
                    } catch (Exception e) {

                    }

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addMessages(messages);
                        mRecyclerView.smoothScrollToPosition(messages.size());
                        mRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 发送消息
     */
    private void send() {
        String msg = mInput.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            try {
                mService.getPubNub().publish().channel(mCurrentChannel).message(GsonUtil.toMap(createMessage(msg))).shouldStore(true).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (status.isError()) {
                            Toast.makeText(ChatRoomActivity.this, "Send operation failed:" + status.getOperation() + ", category:" + status.getCategory() + ", statusCode:" + status.getStatusCode(), Toast.LENGTH_LONG).show();
                        }

                        PLog.d(TAG, "send.operation:" + status.getOperation() + ", category:" + status.getCategory() + ", statusCode:" + status.getStatusCode());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mInput.setText("");
        }
    }

    private JSONObject createMessage(String msg) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("content", msg);
            obj.put("user_name", mUserName);
            obj.put("date", DateUtil.date2Str(new Date()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mService.removeMessageCallback(this);
        unbindService(mConnection);
    }

    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        //通道中有发布消息，则会回调该方法。
        PLog.d(TAG, "message(): message:" + message.getMessage().toString());
        //解析消息内容，并将消息添加到消息列表上
        final Message messag = GsonUtil.parserJsonToArrayBean(message.getMessage().toString(), Message.class);
        messag.isMe = PubNubApplication.sUserName.equals(messag.user_name);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.addMessage(messag);
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });
    }
}
