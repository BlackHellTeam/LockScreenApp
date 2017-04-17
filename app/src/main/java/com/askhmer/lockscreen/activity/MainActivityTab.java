package com.askhmer.lockscreen.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.askhmer.lockscreen.R;
import com.askhmer.lockscreen.fragment.FourFragment;
import com.askhmer.lockscreen.fragment.OneFragment;
import com.askhmer.lockscreen.fragment.ThreeFragment;
import com.askhmer.lockscreen.fragment.TwoFragment;
import com.askhmer.lockscreen.utils.LockscreenService;
import com.askhmer.lockscreen.utils.MutiLanguage;
import com.askhmer.lockscreen.utils.SharedPreferencesFile;

import java.util.ArrayList;
import java.util.List;

public class MainActivityTab extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private SharedPreferencesFile mSharedPref;
    private boolean startService;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*startup set lanaguage*/
        new MutiLanguage(getApplicationContext(),this).StartUpCheckLanguage();

        setContentView(R.layout.activity_main_activity_tab);

        mSharedPref  = new SharedPreferencesFile(getApplicationContext(),SharedPreferencesFile.FILE_INFORMATION_TEMP);
        startService = mSharedPref.getBooleanSharedPreference(SharedPreferencesFile.SERVICELOCK);

        /*start service*/
        if (startService == false) {
            startService(new Intent(this, LockscreenService.class));
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.select();

        setupTabIcons();
       /* tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.white);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.btn_tap_off);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );*/

    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_home,
                R.drawable.ic_portal,
                R.drawable.ic_search,
                R.drawable.ic_setting
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new OneFragment(), "Add friend");
        adapter.addFrag(new ThreeFragment(), "Chat");
        adapter.addFrag(new TwoFragment(), "Profile");
        adapter.addFrag(new FourFragment(), "Other");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
    }


    //Enable backward
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        try {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK: onBackPressed();
            }
        } catch (NullPointerException e) {

        } catch (Exception e1) {

        }
        return false;
    }

    public void onBackPressed() {
        TwoFragment twoFragment = (TwoFragment) adapter.getItem(2);
        if (viewPager.getCurrentItem() == 2 && twoFragment.canGoOrNot()) {
            return;
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* mSharedPref  = SharedPreferencesFile.newInstance(this, SharedPreferencesFile.FILE_INFORMATION_TEMP);*/

        // Restore preferences_n
       /* boolean lockScreen = mSharedPref.getBooleanSharedPreference(SharedPreferencesFile.PREFER_KEY);
        int numVerBeforeUpdate = mSharedPref.getIntSharedPreference(SharedPreferencesFile.KEY_VERSION_APP);
        int currentVersionApp = new CheckVersionCode().checkVersionCode(getApplicationContext());
        Intent i = new Intent(MainActivityTab.this, LockScreenActivity.class);*/

        /*if (numVerBeforeUpdate == 0) {
            mSharedPref.putIntSharedPreference(SharedPreferencesFile.KEY_VERSION_APP,
                    new CheckVersionCode().checkVersionCode(getApplicationContext()));
        }else {
            if (numVerBeforeUpdate < currentVersionApp) {
                mSharedPref.putIntSharedPreference(SharedPreferencesFile.KEY_VERSION_APP,currentVersionApp);
                startActivity(i);
            }
        }*/

        /*if(!lockScreen==true) {
            startActivity(i);
        }*/
        /*if (startService == false) {
            startService(new Intent(this, LockscreenService.class));
        }*/
    }
}
