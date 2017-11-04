package com.cwtcn.pubsub.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by leizhiheng on 2017/10/31.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<String> mTitles;
    private List<Fragment> mFragments;

    public ViewPagerAdapter(FragmentManager fm, List<String> titles, List<Fragment> fragments) {
        super(fm);
        this.mTitles = titles;
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mTitles == null ? 0 : mTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
