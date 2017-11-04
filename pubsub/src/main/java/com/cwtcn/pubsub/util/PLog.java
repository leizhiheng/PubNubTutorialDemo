package com.cwtcn.pubsub.util;

import android.util.Log;

import com.cwtcn.pubsub.Contants;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public class PLog {
    public static final String TAG = "PubNub";

    public static void d(String tag, String msg) {
        if (Contants.OPEN_LOG) Log.d(TAG, tag + "--" + msg);
    }
    public static void i(String tag, String msg) {
        if (Contants.OPEN_LOG) Log.i(TAG, tag + "--" + msg);
    }
    public static void e(String tag, String msg) {
        if (Contants.OPEN_LOG) Log.e(TAG, tag + "--" + msg);
    }
    public static void w(String tag, String msg) {
        if (Contants.OPEN_LOG) Log.w(TAG, tag + "--" + msg);
    }
}
