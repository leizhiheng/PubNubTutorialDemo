package com.cwtcn.pubsub.dao;

import com.cwtcn.pubsub.Bean.Channel;
import com.cwtcn.pubsub.Bean.ChannelDao;
import com.cwtcn.pubsub.PubNubApplication;

import java.util.List;

/**
 * Created by leizhiheng on 2017/10/31.
 */

public class ChannelsDao {
    //添加数据，如有重复，则覆盖
    public static void insertChannel(Channel channel) {
        PubNubApplication.getDaoSession().getChannelDao().insertOrReplace(channel);
    }
    /**
     * 删除数据
     */
    public static void deleteChannel(long id) {
        PubNubApplication.getDaoSession().getChannelDao().deleteByKey(id);
    }

    /**
     * 更新数据
     * @param channel
     */
    public static void updateChannel(Channel channel) {
        PubNubApplication.getDaoSession().getChannelDao().update(channel);
    }
    //根据subscribed的值查询数据
    public static List<Channel> queryChannels(boolean subscribed) {
        return PubNubApplication.getDaoSession().getChannelDao().queryBuilder()
                .where(ChannelDao.Properties.Subscribed.eq(subscribed)).list();
    }

    /**
     * 根据通道名查询channel list
     * @param channelName
     * @return
     */
    public static List<Channel> queryChannels(String channelName) {
        return  PubNubApplication.getDaoSession().getChannelDao().queryBuilder().where(ChannelDao.Properties.Channel_name.eq(channelName)).list();
    }

    /**
     * 查询所有channel
     * @return
     */
    public static List<Channel> queryAll() {
        return PubNubApplication.getDaoSession().getChannelDao().loadAll();
    }

    /**
     * 获取表中记录数
     * @return
     */
    public static long queryCount() {
        return PubNubApplication.getDaoSession().getChannelDao().queryBuilder().count();
    }
}
