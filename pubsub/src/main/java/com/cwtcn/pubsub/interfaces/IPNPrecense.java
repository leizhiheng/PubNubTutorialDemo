package com.cwtcn.pubsub.interfaces;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

/**
 * Created by leizhiheng on 2017/11/1.
 */

public interface IPNPrecense {
    public void presence(PubNub pubnub, PNPresenceEventResult presence);
}
