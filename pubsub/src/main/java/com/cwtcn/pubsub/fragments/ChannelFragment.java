package com.cwtcn.pubsub.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cwtcn.pubsub.Bean.Channel;
import com.cwtcn.pubsub.MainActivity;
import com.cwtcn.pubsub.R;
import com.cwtcn.pubsub.adapter.ChannelItemAdapter;
import com.cwtcn.pubsub.dao.ChannelsDao;
import com.cwtcn.pubsub.events.DBChangeEvent;
import com.cwtcn.pubsub.events.SubscribeAllEvent;
import com.cwtcn.pubsub.util.PLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ChannelFragment extends Fragment {

    public static final String TAG = ChannelFragment.class.getSimpleName();
    private static final String ARG_IS_SUBSCRIBED = "is_subscribed";
    private boolean mIsSubscribed;

    private RecyclerView mRecyclerView;
    private ChannelItemAdapter mAdapter;
    private List<Channel> mChannels;

    private boolean mIsDBHasModified;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChannelFragment() {
    }

    public static ChannelFragment newInstance(boolean isSubscribed) {
        ChannelFragment fragment = new ChannelFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_SUBSCRIBED, isSubscribed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsSubscribed = getArguments().getBoolean(ARG_IS_SUBSCRIBED);
            Log.d(TAG, "onCreate.mIsSubscribed =" + mIsSubscribed );
        }
        EventBus.getDefault().register(this);
    }

    /**
     * 接收到数据库改变的事件后，刷新列表
     * @param event
     */
    @Subscribe
    public void onEventMainThread(DBChangeEvent event) {
        PLog.d(TAG, "onEventMainThread.DBChangeEvent");
        //为了防止频繁刷新列表，将直接下载列表改变修改标志位。然后在onResume中根据标志位判断是否需要刷新列表
        refreshData();
        //mIsDBHasModified = true;
    }

    /**
     * 接收到数据库改变的事件后，刷新列表
     * @param event
     */
    @Subscribe
    public void onEventMainThread(SubscribeAllEvent event) {
        subscribeChannels(mChannels);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.set(0,0,0,2);
                }
            });
            mAdapter = new ChannelItemAdapter(getActivity(), queryChannels());
            mRecyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsDBHasModified) {
            refreshData();
        }
    }

    public void refreshData() {
        mAdapter.refreshData(queryChannels());
    }

    private List<Channel> queryChannels() {
        mChannels = ChannelsDao.queryChannels(mIsSubscribed);
        return mChannels;
    }

    /**
     * 如果是之前订阅过的Channel，并且没有取消订阅。则在这里重新订阅一次
     */
    private void subscribeChannels(List<Channel> channels) {
        List<String> names = new ArrayList<>();
        for (Channel channel: channels) {
            names.add(channel.getChannel_name());
        }
        //MainActivity activity = (MainActivity) getActivity();
        //activity.getService().subscribe(names);
    }
}
