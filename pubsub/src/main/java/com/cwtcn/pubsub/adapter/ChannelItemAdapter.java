package com.cwtcn.pubsub.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwtcn.pubsub.Bean.Channel;
import com.cwtcn.pubsub.ChatRoomActivity;
import com.cwtcn.pubsub.MainActivity;
import com.cwtcn.pubsub.R;

import java.util.Arrays;
import java.util.List;

public class ChannelItemAdapter extends RecyclerView.Adapter<ChannelItemAdapter.ViewHolder> {
    public static final String TAG = ChannelItemAdapter.class.getSimpleName();
    private final List<Channel> mValues;
    private MainActivity mActivity;

    public ChannelItemAdapter(Activity activity, List<Channel> items) {
        mActivity = (MainActivity) activity;
        mValues = items;
        Log.d(TAG, "Constructor.mValues.size = " + mValues.size());
    }

    /**
     * 刷新列表
     * @param items
     */
    public void refreshData(List<Channel> items) {
        mValues.clear();
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_channel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        Log.d(TAG, "Channel toString:" + holder.mItem.toString());
        holder.mNameView.setText("Channel Name:" + mValues.get(position).getChannel_name());
        holder.mContentView.setText("User Count:" +String.valueOf(mValues.get(position).getUser_count()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.setCurrentChannel(mValues.get(position));
                if (mValues.get(position).getSubscribed()) {
                    //订阅过则跳转到聊天室
                    ChatRoomActivity.start(mActivity, mValues.get(position).getChannel_name());
                } else {
                    //如果没订阅过，则订阅
                    subscribe(mValues.get(position));
                }
            }
        });
    }

    /**
     * 订阅channel,订阅结果会回调PubNub的Listenr的status()方法中
     * @param channel
     */
    private void subscribe(final Channel channel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("订阅").setMessage("点击确认按钮，订阅该Channel并跳转到聊天室");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "subscribe channel :" + channel.getChannel_name());
                mActivity.getService().getPubNub().subscribe().withPresence().channels(Arrays.asList(channel.getChannel_name())).execute();
                ChatRoomActivity.start(mActivity, channel.getChannel_name());
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mContentView;
        public Channel mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
