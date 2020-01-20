package com.unexceptional.beast.banko.newVersion.ui.activity;
/*
 * CALL IT DONE 1.11.2018
 */

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivityWelcomeBinding;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WelcomeActivity extends BasicActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ImageView[] indicators;
    protected ActivityWelcomeBinding mBinding;
    /**
     * mPage and mSize controls activity (indicators, colors etc) when fragment number controls what we see only
     *
     */
    int mPage = 0;   //  to track page position
    int mSize =5;

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }catch (Exception e){

        }

        mBinding= (ActivityWelcomeBinding) dataBinding;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
            mBinding.introBtnNext.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.outline_keyboard_arrow_right_white_24));

       setViewPager();

       setClicks();
    }

    private void setClicks(){
        mBinding.introBtnNext.setOnClickListener(v -> {
            mPage++;
            mBinding.viewPager.setCurrentItem(mPage, true);
        });


        mBinding.introBtnSkip.setOnClickListener(v -> goToNextActivity());

        mBinding.introBtnFinish.setOnClickListener(v -> goToNextActivity());
    }

    private void goToNextActivity(){
        preferences.edit().putBoolean(getResources().getString(R.string.key_welcome_undone), false).apply();
        startActivity(new Intent(WelcomeActivity.this, WelcomeSettingsActivity.class));
        finish();
    }

    void setViewPager(){
        indicators = new ImageView[]{mBinding.introIndicator0, mBinding.introIndicator1, mBinding.introIndicator2,
                mBinding.introIndicator3, mBinding.introIndicator4, mBinding.introIndicator5};

        for (int i = 0; i< mSize ; i++)
            indicators[i].setVisibility(View.VISIBLE);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);

        mBinding.viewPager.setCurrentItem(mPage);
        updateIndicators(mPage);
        final int color1 = ContextCompat.getColor(this, R.color.account_blue);
        final int color2 = ContextCompat.getColor(this, R.color.blue);
        final int color3 = ContextCompat.getColor(this, R.color.light_blue);
        final int color4 = ContextCompat.getColor(this, R.color.cyan);
        final int color5 = ContextCompat.getColor(this, R.color.teal);
        final int color6 = ContextCompat.getColor(this, R.color.indigo);

        final int[] colorList = new int[]{color1, color2, color3, color4, color5, color6};

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*
                color update
                 */
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == mSize -1 ? position : position + 1]);
                mBinding.viewPager.setBackgroundColor(colorUpdate);

            }

            @Override
            public void onPageSelected(int position) {

                mPage = position;

                updateIndicators(mPage);

                switch (position) {
                    case 0:
                        mBinding.viewPager.setBackgroundColor(color1);
                        break;
                    case 1:
                        mBinding.viewPager.setBackgroundColor(color2);
                        break;
                    case 2:
                        mBinding.viewPager.setBackgroundColor(color3);
                        break;
                    case 3:
                        mBinding.viewPager.setBackgroundColor(color4);
                        break;
                    case 4:
                        mBinding.viewPager.setBackgroundColor(color5);
                        break;
                    case 5:
                        mBinding.viewPager.setBackgroundColor(color6);
                        break;
                }

                mBinding.introBtnNext.setVisibility(position == mSize -1 ? View.GONE : View.VISIBLE);
                mBinding.introBtnFinish.setVisibility(position == mSize -1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        @BindView(R.id.section_img) ImageView mImg;
        @BindView(R.id.section_label) TextView mLabel;
        @BindView(R.id.section_description) TextView mDescription;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private void setFragmentSpecifics(String title, int imageId, String description){
            mLabel.setText(title);
            mImg.setBackgroundResource(imageId);
            mDescription.setText(description);
        }

        private void getFragmentSpecifics(){
            if (getArguments() != null) {

                switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                    case 1:// welcome in app
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_1), R.drawable.welcome_activity_welcome, getString(R.string.welcome_activity_description_1));
                        break;
                    case 2:// widget
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_2), R.drawable.welcome_activity_transactions, getString(R.string.welcome_activity_description_2));
                        break;
                    case 3:// bank info notifications etc
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_3), R.drawable.welcome_activity_notifications, getString(R.string.welcome_activity_description_3));
                        break;
                    case 4://categoryIds, charts and filter
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_4), R.drawable.welcome_activity_charts, getString(R.string.welcome_activity_description_4));
                        break;
                    case 5://currencies
                        setFragmentSpecifics(getString(R.string.welcome_activity_title_5), R.drawable.welcome_activity_currencies, getString(R.string.welcome_activity_description_5));
                        break;

                }
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
            ButterKnife.bind(this, rootView);
            getFragmentSpecifics();
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given mPage.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);

        }

        @Override
        public int getCount() {
            return mSize;
        }
    }
}