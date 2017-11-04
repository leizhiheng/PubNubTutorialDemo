package com.cwtcn.pubsub.Bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.cwtcn.pubsub.Contants;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public class Message {
    public String user_name;
    public String content;
    public String date;
    public boolean isMe;

    public Message(String name, String content, String date, boolean isMe) {
        this.user_name = name;
        this.content = content;
        this.date = date;
        this.isMe = isMe;
    }
}
