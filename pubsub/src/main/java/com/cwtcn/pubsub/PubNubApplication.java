package com.cwtcn.pubsub;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import com.cwtcn.pubsub.Bean.Channel;
import com.cwtcn.pubsub.Bean.DaoMaster;
import com.cwtcn.pubsub.Bean.DaoSession;
import com.cwtcn.pubsub.dao.ChannelsDao;
import com.cwtcn.pubsub.service.PNAdminService;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leizhiheng on 2017/10/31.
 */

public class PubNubApplication extends Application {
    private static DaoSession daoSession;
    public static String sUserName;
    public static String sUuid;
    private static PubNub mAdminPubnub;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
        setDBDataFirstTime();
        SharedPreferences preferences = getSharedPreferences(Contants.PREF_NAME, MODE_PRIVATE);
        sUserName = preferences.getString(Contants.PREF_KEY_USER_NAME, "");
        sUuid = preferences.getString(Contants.PREF_KEY_UUID, "");
        //获取授权服务
        bindAdminService();
    }

    private void setupDatabase() {
        //创建数据库pubnub.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "pubnub.db", null);
        //获取可写数据库
        SQLiteDatabase database = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(database);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
    }

    private void bindAdminService() {
        Intent intent = new Intent(this, PNAdminService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PNAdminService.PNAdminBinder binder = (PNAdminService.PNAdminBinder) service;
            mAdminPubnub = binder.getService().getPubNub();

            grantAppLevel(true, true);
            //grantChannelLevel(true, true);
            //grantUserLevel(true, true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 获取用于权限管理的PubNub对象
     * @return
     */
    public static PubNub getAdminPubnub() {
        return mAdminPubnub;
    }

    /**
     * 释放PubNubd
     */
    public static void releaseAdminPubnub() {
        mAdminPubnub.stop();
        mAdminPubnub = null;
    }

    /**
     * 在Application级别进行授权
     * @param read
     * @param write
     */
    public static void grantAppLevel(boolean read, boolean write) {
        mAdminPubnub.grant().read(read).write(write).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {

                }
                //grantChannelLevel(true, true);
            }
        });
    }

    /**
     * 在Channel级别进行授权
     * @param read
     * @param write
     */
    public static void grantChannelLevel(boolean read, boolean write) {
        List<Channel> all = ChannelsDao.queryAll();
        List<String> names = new ArrayList<>();
        for (Channel c: all) {
            names.add(c.getChannel_name());
        }
        mAdminPubnub.grant().read(read).write(write).channels(names).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {

                }
                //登录用户被授予所有通道的read权限。
                grantUserLevel(true, true);
            }
        });
    }

    /**
     * 用户登录后默认被授予read权限，否则用户无法订阅。获取history,precense
     */
    public static void grantUserLevel(boolean read, boolean write) {
        List<Channel> all = ChannelsDao.queryAll();
        List<String> names = new ArrayList<>();
        for (Channel c: all) {
            names.add(c.getChannel_name());
        }
        mAdminPubnub.grant().read(read).write(write).channels(names).authKeys(Arrays.asList(sUuid)).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status.isError()) {

                }
            }
        });
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    private void setDBDataFirstTime() {
        List<Channel> channelList = ChannelsDao.queryAll();
        if (channelList == null || channelList.size() == 0) {
            new InsetDefaultChannelsTask().execute();
        }
    }

    class InsetDefaultChannelsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            ChannelsDao.insertChannel(new Channel(0, Contants.CHANNEL_COMMEN, 0, false));
            String [] defaultChannels = getResources().getStringArray(R.array.default_channels);
            for (int i = 0; i < defaultChannels.length; i++) {
                ChannelsDao.insertChannel(new Channel((i + 1), defaultChannels[i], 0, false));
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
