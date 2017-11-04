package com.cwtcn.pubsub.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.cwtcn.pubsub.Contants;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.UUID;

/**
 * 这个服务用于模拟远程服务器，管理一个拥有Access Manager 的PubNub对象，这个PubNub对象只用于对其它用户授权
 */
public class PNAdminService extends Service {

    private PubNub mPubNub;
    public PNAdminService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPubNub();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PNAdminBinder();
    }

    public PubNub getPubNub() {
        return mPubNub;
    }

    public class PNAdminBinder extends Binder {
        public PNAdminService getService() {
            return PNAdminService.this;
        }
    }

    /**
     * 初始化PubNub对象
     */
    private void initPubNub() {
        PNConfiguration configuration = new PNConfiguration();
        //如果不用Publish，可以只设置这个Key
        configuration.setSubscribeKey(Contants.SUB_KEY);
        configuration.setPublishKey(Contants.PUB_KEY);
        //只有开启Access Manager功能时才需要这个。设置这个Key后就可以对Channel和用户进行去权限管理。所以这个Key应该在服务端保存而不是移动端。
        configuration.setSecretKey(Contants.SECRET_KEY);
        //是否使用SSL,默认为true
        configuration.setSecure(true);

        mPubNub = new PubNub(configuration);
        //这个PubNub只用于授权，所以不需要设置Listener
        //addPNListener();
    }

    /**
     * 为PubNub添加监听器
     */
    private void addPNListener() {
        mPubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                //如果用户subscribe/unsubscribe channel则通知ChannelFragment,数据库发生改变。
            }
        });
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
