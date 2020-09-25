package urbantrees.spaklingscience.at.urbantrees.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.fragments.IntroBluetoothPermissionFragment;
import urbantrees.spaklingscience.at.urbantrees.fragments.IntroFragment;
import urbantrees.spaklingscience.at.urbantrees.fragments.IntroGenericFragment;
import urbantrees.spaklingscience.at.urbantrees.fragments.IntroMainFragment;
import urbantrees.spaklingscience.at.urbantrees.util.PreferenceManager;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

public class IntroActivity extends FragmentActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int numPages;
    private Button btnNext;
    private PreferenceManager prefManager;

    private boolean isTreeDataCollectEnabled = true;
    private boolean isConnectivityPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstLaunch() &&
                (!prefManager.isTreeDataCollect() ||
                IntroBluetoothPermissionFragment.isPermissionsGranted(this))) {
            launchHomeScreen();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_intro);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnNext = (Button) findViewById(R.id.btn_next);

        myViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());


        this.isConnectivityPermissionsGranted = Utils.isPermissionsGranted(this, IntroBluetoothPermissionFragment.NEEDED_PERMISSIONS);
        updateNavigation(0);

        // making notification bar transparent
        changeStatusBarColor();

        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < myViewPagerAdapter.getCount()) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    private void updateNavigation(int currentPage) {

        this.dots = new TextView[myViewPagerAdapter.getCount()];

        int dotsColorInactive = getResources().getColor(R.color.colorTextLight);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&ndash;"));
            dots[i].setTextSize(35);
            dots[i].setAlpha(0.2f);
            dots[i].setTextColor(dotsColorInactive);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setAlpha(0.8f);
        }

        if (myViewPagerAdapter.getCachedItem(currentPage) != null) {
            btnNext.setVisibility(((IntroFragment) myViewPagerAdapter.getCachedItem(currentPage)).canContinue() ? View.VISIBLE : View.INVISIBLE);
        }

        if (currentPage == IntroActivity.this.myViewPagerAdapter.getCount() - 1) {
            btnNext.setText(getString(R.string.intro_nav_done));
        } else {
            btnNext.setText(getString(R.string.intro_nav_next));
        }

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstLaunch(false);
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void setTreeDataCollectEnabled(boolean collect) {
        this.prefManager.setTreeDataCollect(collect);
        this.isTreeDataCollectEnabled = collect;
        updateNavigation(0);
        myViewPagerAdapter.notifyChangeInPosition(1);
        myViewPagerAdapter.notifyDataSetChanged();
    }

    public void setConnectivityPermissionsGranted(boolean granted) {
        this.isConnectivityPermissionsGranted = granted;
        updateNavigation(1);
        myViewPagerAdapter.notifyDataSetChanged();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            updateNavigation(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
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

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] items = new Fragment[0];
        private int currentPosition;
        private long baseId = 0;

        public MyViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public Fragment getCachedItem(int position) {
            return this.items.length > position ? this.items[position] : null;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            this.currentPosition = position;
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return FragmentPagerAdapter.POSITION_NONE;
        }


        @Override
        public long getItemId(int position) {
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        public void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {

            Fragment newFragment = null;
            switch (position) {
                case 0:
                    newFragment = IntroMainFragment.newInstance(isTreeDataCollectEnabled);
                    break;
                case 1:
                    if (isTreeDataCollectEnabled) {
                        newFragment = IntroBluetoothPermissionFragment.newInstance();
                    } else {
                        newFragment = IntroGenericFragment.newInstance(R.layout.fragment_intro_done);
                    }
                    break;
                case 2:
                    newFragment = IntroGenericFragment.newInstance(R.layout.fragment_intro_done);
                    break;
            }
            this.items[position] = newFragment;
            return newFragment;

        }

        @Override
        public int getCount() {

            int pages = 3;
            if (!IntroActivity.this.isConnectivityPermissionsGranted) {
                pages -= 1;
            }
            if (!IntroActivity.this.isTreeDataCollectEnabled) {
                pages = 2;
            }
            this.items = Arrays.copyOf(this.items, pages);
            return pages;

        }

    }
}
