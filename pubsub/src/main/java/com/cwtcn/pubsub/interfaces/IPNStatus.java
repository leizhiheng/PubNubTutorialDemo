package com.cwtcn.pubsub.interfaces;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.PNStatus;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public interface IPNStatus {
    public void status(PubNub pubnub, PNStatus status);
}
