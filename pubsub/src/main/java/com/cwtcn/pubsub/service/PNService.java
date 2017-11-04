package com.cwtcn.pubsub.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.cwtcn.pubsub.Contants;
import com.cwtcn.pubsub.events.DBChangeEvent;
import com.cwtcn.pubsub.interfaces.IPNMessage;
import com.cwtcn.pubsub.interfaces.IPNPrecense;
import com.cwtcn.pubsub.interfaces.IPNStatus;
import com.cwtcn.pubsub.util.PLog;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PNService extends Service {
    public static final String TAG = PNService.class.getSimpleName();
    private PubNub mPubNub;
    private Binder mBinder;
    private List<IPNPrecense> mPrecenseCallbacks = new ArrayList<>();
    private List<IPNMessage> mMessageCallbacks = new ArrayList<>();
    private List<IPNStatus> mStatusCallbacks = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new PNBinder();
        EventBus.getDefault().register(this);
        initPubNub();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class PNBinder extends Binder {
        public PNService getPNService() {
            return PNService.this;
        }
    }

    /**
     * 获取PubNub对象
     * @return
     */
    public PubNub getPubNub() {
        if (mPubNub == null) {
            initPubNub();
        }
        return mPubNub;
    }
    /**
     * 初始化PubNub对象
     */
    private void initPubNub() {
        PNConfiguration configuration = new PNConfiguration();
        //如果不用Publish，可以只设置这个Key
        configuration.setSubscribeKey(Contants.SUB_KEY);
        configuration.setPublishKey(Contants.PUB_KEY);
        //Uuid用于标识用户，可以是一个用户名,也可以是一个随机的UUID字符串.如果没有传入，会自动生成一个默认的uuid.
        configuration.setUuid(getUuid());
        //只有开启Access Manager功能时才需要这个。设置这个Key后就可以对Channel和用户进行去权限管理。所以这个Key应该在服务端保存而不是移动端。
        //configuration.setSecretKey(Contants.SECRET_KEY);
        //如果开启了Access Manager功能，Client在请求时需要传入该Auth key。这个key也是一个自定义的字符创。例如：可以直接使用uuid
        configuration.setAuthKey(getUuid());
        //是否使用SSL,默认为true
        configuration.setSecure(true);

        mPubNub = new PubNub(configuration);
        addPNListener();
    }

    /**
     * 获取UUID
     * @return
     */
    private String getUuid() {
        SharedPreferences preferences = getSharedPreferences(Contants.PREF_NAME, MODE_PRIVATE);
        String uuid = preferences.getString(Contants.PREF_KEY_UUID, "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            preferences.edit().putString(Contants.PREF_KEY_UUID, uuid).commit();
        }
        return uuid;
    }

    /**
     * 为PubNub添加监听器
     */
    private void addPNListener() {
        mPubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                PLog.d(TAG, "==>listenr.status");
                //订阅结果
                for (IPNStatus callback : mStatusCallbacks) {
                    if (callback != null) callback.status(pubnub, status);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                PLog.d(TAG, "==>listenr.message");
                //接收消息
                for (IPNMessage callback : mMessageCallbacks) {
                    if (callback != null) callback.message(pubnub, message);
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                //如果用户subscribe/unsubscribe channel则通知ChannelFragment,数据库发生改变。
                if (presence.getEvent().equals("leave") || presence.getEvent().equals("join")) {
                    EventBus.getDefault().post(new DBChangeEvent());
                }

                //用户状态改变
                for (IPNPrecense callback : mPrecenseCallbacks) {
                    if (callback != null && callback instanceof IPNPrecense) callback.presence(pubnub, presence);
                }
            }
        });
    }

    @Subscribe
    public void onEvent(String text) {
        PLog.d(TAG, "onEventMainThread.DBChangeEvent");
    }

    /**
     * 添加Precense事件回调对象
     * @param callback
     */
    public void addPreceseCallback(IPNPrecense callback) {
        this.mPrecenseCallbacks.add(callback);
    }

    /**
     * 删除Precense事件回调对象
     * @param callback
     */
    public void removePrecenseCallback(IPNPrecense callback) {
        if (mPrecenseCallbacks.contains(callback)) mPrecenseCallbacks.remove(callback);
    }

    /**
     * 添加Status事件回调对象
     * @param callback
     */
    public void addStatusCallback(IPNStatus callback) {
        this.mStatusCallbacks.add(callback);
    }

    /**
     * 删除Status事件回调对象
     * @param callback
     */
    public void removeStatusCallback(IPNStatus callback) {
        if (mStatusCallbacks.contains(callback)) mStatusCallbacks.remove(callback);
    }

    /**
     * 添加Message事件回调对象
     * @param callback
     */
    public void addMessageCallback(IPNMessage callback) {
        this.mMessageCallbacks.add(callback);
    }

    /**
     * 删除Message事件回调对象
     * @param callback
     */
    public void removeMessageCallback(IPNMessage callback) {
        if (mMessageCallbacks.contains(callback)) mMessageCallbacks.remove(callback);
    }

    /**
     * 订阅通道
     * @param channels
     */
    public void subscribe(List<String> channels) {
        mPubNub.subscribe().withPresence().channels(channels).execute();
    }
    /**
     * 释放PubNub对象
     */
    public void releasePubNub() {
        //mPubNub.removeListener(Listener);
        mPubNub.stop();
        mPubNub = null;
    }
}
