package com.medoapps.www.onlinequran;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.medoapps.www.onlinequran.fragment.ThemesFragment;
import com.medoapps.www.onlinequran.service.AuthService;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    public static Button btnSkip, btnNext;
    private PreferenceManager prefManager;
    private LinearLayout fragmentContainer;
    ThemesFragment newFragment ;
    AuthService authService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService(WelcomeActivity.this);
        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
        SettingSaved settingSaved=new SettingSaved(WelcomeActivity.this);
        settingSaved.SaveData();





        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);
//        newFragment = new ThemesFragment();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.slide_screen1,
                R.layout.slide_screen2,
                R.layout.slide_screen3,
                R.layout.slide_screen4};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startReminderService();

                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
//                    startReminderService();
                    launchHomeScreen();
                }
            }
        });

    }

    private void startReminderService(){
        startService(new Intent(WelcomeActivity.this, QuranListenTimerService.class));
    }
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        /*SettingSaved sv = new SettingSaved(getApplicationContext());
        sv.LoadData();*/
        prefManager.setFirstTimeLaunch(false);
        SettingSaved settingSaved=new SettingSaved(WelcomeActivity.this);
        settingSaved.LoadData();
        settingSaved.SaveData();



        try {
            if (!authService.isUserSignedIn()){
                boolean isAuthed = authService.signInAnonymously();
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();

            }else{

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }

        //startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
        //finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
//            startLoadThemesFragment(position);
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                //btnNext.setVisibility(View.GONE);
                btnSkip.setVisibility(View.GONE);


            } else {
                // still pages are left
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setText(getString(R.string.next));
                //btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void darkMode(View view) {
        Intent down = new Intent(WelcomeActivity.this, ThemesActivity.class);
        startActivity(down);
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        private FrameLayout fragmentContainer;
        FragmentManager fm = getSupportFragmentManager();

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            try {

                layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View view = layoutInflater.inflate(layouts[position], container, false);
                container.addView(view);
                Log.d("TAG", "instantiateItem: " + position);

                fragmentContainer = view.findViewById(R.id.welcomefragmentContainer);

                try {
                    if (fragmentContainer !=null){
                        newFragment = new ThemesFragment();

                        if (newFragment != null) {
        //                    fm.beginTransaction().remove(newFragment).commit();
                        }
                        fm.beginTransaction()
                                .replace(R.id.welcomefragmentContainer, newFragment, "ThemesFragment")
                                .addToBackStack(null)
                                .commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return view;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    public void buset(View view){

        Intent intent = new Intent(getApplicationContext(),TimePicker.class);
        startActivity(intent);
        //btnNext.setVisibility(View.VISIBLE);

    }
}