package com.cwtcn.pubsub.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by leizhiheng on 2017/10/31.
 */

@Entity
public class Channel {
    @Id(autoincrement = true)
    private long id;
    //通道名称
    @Unique
    private String channel_name;
    private int user_count;
    //是否已经订阅过
    private boolean subscribed;

    @Generated(hash = 1473903396)
    public Channel(long id, String channel_name, int user_count, boolean subscribed) {
        this.id = id;
        this.channel_name = channel_name;
        this.user_count = user_count;
        this.subscribed = subscribed;
    }

    @Generated(hash = 459652974)
    public Channel() {
    }

    @Override
    public String toString() {
        return "Channel name:" + channel_name + ", id:" + id + ", userCount:" + user_count + ", subscribed:" + subscribed;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getChannel_name() {
        return this.channel_name;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public int getUser_count() {
        return this.user_count;
    }

    public void setUser_count(int user_count) {
        this.user_count = user_count;
    }

    public boolean getSubscribed() {
        return this.subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
