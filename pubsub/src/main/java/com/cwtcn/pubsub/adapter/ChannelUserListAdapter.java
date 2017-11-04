package com.cwtcn.pubsub.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cwtcn.pubsub.Bean.User;
import com.cwtcn.pubsub.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leizhiheng on 2017/11/2.
 */
public class ChannelUserListAdapter extends RecyclerView.Adapter<ChannelUserListAdapter.ViewHolder> {

    private List<User> mUsers;
    private Context mContext;
    private LayoutInflater mInflater;

    private int mCurrentPosition;

    public interface OnGrantClickedlistener {
        void grantRead(User user);
        void grantWrite(User user);
    }

    private OnGrantClickedlistener mListener;

    public void setOnGrantClickedlistener(OnGrantClickedlistener listener) {
        this.mListener = listener;
    }

    public ChannelUserListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        mUsers = new ArrayList<User>();
    }

    /**
     * 重新刷新用户列表
     * @param users
     */
    public void refreshUsers(List<User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    /**
     * 修改某个用户属性后，刷新列表
     * @param user
     */
    public void refreshUser(User user) {
        mUsers.remove(mCurrentPosition);
        mUsers.add(mCurrentPosition, user);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(mInflater.inflate(R.layout.adapter_channel_user_list_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindView(mUsers.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUserName, mState;
        private Button mRead, mWrite;

        public ViewHolder(View itemView) {
            super(itemView);
            mState = itemView.findViewById(R.id.state);
            mUserName = itemView.findViewById(R.id.user_name);
            mRead = itemView.findViewById(R.id.read_set);
            mWrite = itemView.findViewById(R.id.write_set);
        }

        public void bindView(final User user, final int position) {
            mUserName.setText(user.mUserName + ":" + user.mUserId);
            mState.setText("状态:" + user.mState);
            if (user.mIsGrantedRead) {
                mRead.setBackgroundResource(R.drawable.button_bg_granted);
            } else {
                mRead.setBackgroundResource(R.drawable.button_bg_ungranted);
            }
            mRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentPosition = position;
                    if (mListener != null) {
                        mListener.grantRead(user);
                    }
                }
            });

            if (user.mIsGrantedWrite) {
                mWrite.setBackgroundResource(R.drawable.button_bg_granted);
            } else {
                mWrite.setBackgroundResource(R.drawable.button_bg_ungranted);
            }
            mWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentPosition = position;
                    if (mListener != null) {
                        mListener.grantWrite(user);
                    }
                }
            });
        }
    }
}
