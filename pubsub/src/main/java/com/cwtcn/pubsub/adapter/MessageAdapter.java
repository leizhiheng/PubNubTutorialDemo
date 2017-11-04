package com.cwtcn.pubsub.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cwtcn.pubsub.Bean.Message;
import com.cwtcn.pubsub.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leizhiheng on 2017/11/1.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    public static final int TYPE_LEFT = 1;
    public static final int TYPE_RIGHT = 2;

    private Context mContext;
    private LayoutInflater mInflater;

    public MessageAdapter(Context context) {
        mMessages = new ArrayList<Message>();
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        switch (viewType) {
            //左侧布局，用于显示其他人的消息
            case TYPE_LEFT:
                holder = new ViewHolder(mInflater.inflate(R.layout.adapter_message_item_left, parent, false));
                break;
            //右侧布局，用于显示用户自己的消息
            case TYPE_RIGHT:
                holder = new ViewHolder(mInflater.inflate(R.layout.adapter_message_item_right, parent, false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindView(mMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).isMe ? TYPE_RIGHT : TYPE_LEFT;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUserName, mContent, mTime;

        public ViewHolder(View itemView) {
            super(itemView);
            mUserName = itemView.findViewById(R.id.user_name);
            mContent = itemView.findViewById(R.id.content);
            mTime = itemView.findViewById(R.id.date);
        }
        public void bindView(Message message) {
            mUserName.setText(message.user_name);
            mContent.setText(message.content);
            mTime.setText(message.date);
        }
    }

    /**
     * 添加一个消息
     * @param msg
     */
    public void addMessage(Message msg) {
        mMessages.add(msg);
        notifyDataSetChanged();
    }

    /**
     * 添加一组消息
     * @param msgs
     */
    public void addMessages(List<Message> msgs) {
        mMessages.addAll(0, msgs);
        notifyDataSetChanged();
    }
}
