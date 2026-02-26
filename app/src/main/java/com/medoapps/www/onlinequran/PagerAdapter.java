package com.medoapps.www.onlinequran;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.medoapps.www.onlinequran.fragment.MyPostsFragment;
import com.medoapps.www.onlinequran.fragment.MyTopPostsFragment;
import com.medoapps.www.onlinequran.fragment.RecentPostsFragment;


public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RecitesName tab1 = new RecitesName();
                return tab1;
            case 1:
                RecentPostsFragment tab2 = new RecentPostsFragment();
                return tab2;
            case 2:
                MyPostsFragment tab3 = new MyPostsFragment();
                return tab3;
            case 3:
                MyTopPostsFragment tab4 = new MyTopPostsFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}