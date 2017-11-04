package com.cwtcn.pubnubdemo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TableLayout;

import com.cwtcn.pubnubdemo.multi.MultiTabFragment;
import com.cwtcn.pubnubdemo.precense.PrecenseTabFragment;
import com.cwtcn.pubnubdemo.pubsub.PubsubTabFragment;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.Arrays;

public class ChatManagerActivity extends FragmentActivity {
    public static final String TAG = ChatManagerActivity.class.getSimpleName();
    private String mUserName = "leizhiheng";
    private PubNub mPubnub_DataStream;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_manager);

        initPubnub();
        initActionBar();
        //initFragment();
        subscribe();

    }

    private void initPubnub() {
        PNConfiguration config = new PNConfiguration();
        config.setPublishKey(Constants.PUBNUB_PUBLISH_KEY);
        config.setSubscribeKey(Constants.PUBNUB_SUBSCRIBE_KEY);
        config.setUuid(this.mUserName);
        config.setSecure(true);
        this.mPubnub_DataStream = new PubNub(config);
        this.mPubnub_DataStream.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                Log.d("test", "category :" + status.getCategory());
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
    }

    private void subscribe() {
        mPubnub_DataStream.subscribe().channels(Arrays.asList("MyChatRoom")).withPresence().execute();
    }
    private void initActionBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("PubnubDemo");
    }
    private void initFragment() {
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(PubsubTabFragment.newInstance("", ""));
        fragments.add(PrecenseTabFragment.newInstance("", ""));
        fragments.add(MultiTabFragment.newInstance("", ""));
        initViewPager(fragments);
    }

    private void initViewPager(ArrayList<Fragment> fragments) {
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new ChatFragmentAdapter(getSupportFragmentManager(), fragments));
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    class ChatFragmentAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> mFragments;
        public ChatFragmentAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Tab-" + position;
        }
    }
}
