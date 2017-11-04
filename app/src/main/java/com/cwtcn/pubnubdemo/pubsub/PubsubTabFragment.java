package com.cwtcn.pubnubdemo.pubsub;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.test.espresso.core.deps.guava.collect.ImmutableMap;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.cwtcn.pubnubdemo.Constants;
import com.cwtcn.pubnubdemo.Model.Message;
import com.cwtcn.pubnubdemo.R;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PubsubTabFragment extends Fragment {
    public static final String TAG = PubsubTabFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText mPubEt;
    private Button mSendBtn;
    private ListView mMsgListView;

    private ListAdapter mAdapter;
    private List<Message> mMessages;

    private PubNub mPubNub;
    private String mUserName = "channel_leizhiheng_1";
    private String mChannel = "channel_leizhiheng_1";
    private List mChannels = new ArrayList<String>();

    public PubsubTabFragment() {
        // Required empty public constructor
    }

    /**
     */
    // TODO: Rename and change types and number of parameters
    public static PubsubTabFragment newInstance(String param1, String param2) {
        PubsubTabFragment fragment = new PubsubTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mChannels.add("channel_leizhiheng_1");
        mChannels.add("channel_leizhiheng_2");
        initPubnub();
    }

    private void initPubnub() {
        PNConfiguration configuration = new PNConfiguration();
        configuration.setPublishKey(Constants.PUBNUB_PUBLISH_KEY);
        configuration.setSubscribeKey(Constants.PUBNUB_SUBSCRIBE_KEY);
        configuration.setUuid(this.mUserName);
        configuration.setSecure(false);
        mPubNub = new PubNub(configuration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pubsub_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
        history();
        subscribe();
        publish();
    }

    private void initData() {
        mMessages = new ArrayList<Message>();
        mAdapter = new ListAdapter(getActivity(), mMessages);
    }
    private void initView(View view) {
        mPubEt = view.findViewById(R.id.pub_message);
        mSendBtn = view.findViewById(R.id.send_button);
        mMsgListView = view.findViewById(R.id.listview_send);

        mMsgListView.setAdapter(mAdapter);
    }

    private void history() {
        //mPubNub.addListener(new PubnubPnCallback());
        mPubNub.history().channel("channel_leizhiheng_1").async(new PNCallback<PNHistoryResult>() {
            @Override
            public void onResponse(PNHistoryResult result, PNStatus status) {
                Log.d(TAG, "history.initChannels.onResponse message size = " + (result == null ? 0 : result.getMessages().size()));

                if (status.getAffectedChannels() == null) return;

                for (String channel: status.getAffectedChannels()) {
                    Log.d(TAG, "history.initChannels.onResponse channel : " + channel);
                }
            }
        });
    }

    private void subscribe() {
        mPubNub.addListener(new PubnubPnCallback());
        mPubNub.subscribe().channels(mChannels).withPresence().execute();
    }

    private void publish() {
        mPubNub.addListener(new PubnubPnCallback());
//        final Map<String, String> message = ImmutableMap.<String, String>of(
//                "sender", this.mUserName,
//                "message", "Hell everyone!",
//                "timestamp", "2017-10-24");

        JSONObject messageJSON = new JSONObject();
        try {
            messageJSON.put("name", "leizhiheng");
            messageJSON.put("age", 23);
            messageJSON.put("sex", "female");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        mPubNub.publish().channel(Constants.USER1).message(messageJSON).async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, PNStatus status) {
                Log.d(TAG, "publish.PNPublishResult timetoken : " + (result == null ? "null" : result.getTimetoken()));
            }
        });
    }

    public class PubnubPnCallback extends SubscribeCallback {

        @Override
        public void status(PubNub pubnub, PNStatus status) {
            Log.d(TAG, "PubnubPnCallback.status() isError = " + status.isError() + ", statuscode = " + status.getStatusCode());
            if (status.getAffectedChannels() == null) return;
            for (String channel: status.getAffectedChannels()) {
                Log.d(TAG, "channel name : " + channel);
            }
        }

        @Override
        public void message(PubNub pubnub, PNMessageResult message) {
            Log.d(TAG, "PubnubPnCallback.message()");
        }

        @Override
        public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            Log.d(TAG, "PubnubPnCallback.presence()");
        }
    }
}

