package com.cwtcn.pubnubdemo.pubsub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cwtcn.pubnubdemo.Model.Message;
import com.cwtcn.pubnubdemo.R;

import java.util.List;

/**
 * Created by leizhiheng on 2017/10/23.
 */

public class ListAdapter extends BaseAdapter {

    public List<Message> mMessages;
    private Context mContext;
    private LayoutInflater mInflater;

    public ListAdapter(Context context, List<Message> messages) {
        this.mContext = context;
        this.mMessages = messages;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.adapter_message_list, null);
        Message message = mMessages.get(position);
        TextView publisherName = convertView.findViewById(R.id.publisher_name);
        TextView publishTime = convertView.findViewById(R.id.publish_time);
        TextView publishContent = convertView.findViewById(R.id.publish_content);

        publisherName.setText(message.mPublisherName);
        publishTime.setText(message.mPublishTime);
        publishContent.setText(message.mPublishContent);
        return convertView;
    }
}
