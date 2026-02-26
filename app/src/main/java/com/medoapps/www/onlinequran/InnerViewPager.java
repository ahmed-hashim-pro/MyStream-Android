package com.medoapps.www.onlinequran;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InnerViewPager#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InnerViewPager extends Fragment {
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    public InnerViewPager() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment
     * @return A new instance of fragment ViewPagerAdapter.
     */
    public static InnerViewPager newInstance() {
        InnerViewPager fragment = new InnerViewPager();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        return view;
    }


    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_ITEMS = 2;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return RecitesName.newInstance();

            }
            else {
                return RecitesName.newInstance();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if(position == 0){
                //return InnerFragment1.PAGE_TITLE;
                return "ll";
            }
            else {
                //return InnerFragment2.PAGE_TITLE;
                return "ff" ;
            }
        }
    }
}
