package com.cwtcn.pubsub.interfaces;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public interface IPNMessage {
    public void message(PubNub pubnub, PNMessageResult message);
}
