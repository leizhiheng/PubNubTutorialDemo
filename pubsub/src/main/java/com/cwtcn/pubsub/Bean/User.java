package com.cwtcn.pubsub.Bean;

/**
 * Created by leizhiheng on 2017/10/31.
 */
public class User {
    public String mUserId;
    public String mUserName;
    public boolean mIsGrantedRead;
    public boolean mIsGrantedWrite;
    public String mState;

    public User(String userId, String userName, boolean isGrantedRead, boolean isGrantedWrite) {
        this.mUserId = userId;
        this.mUserName = userName;
        this.mIsGrantedRead = isGrantedRead;
        this.mIsGrantedWrite = isGrantedWrite;
    }
}
