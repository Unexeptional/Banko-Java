package com.unexceptional.beast.banko.newVersion.ui.activity;
/*
 * CALL IT DONE 1.11.2018
 */

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ActivityWelcomeBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.ui.fragment.TutorialFragment;

import org.jetbrains.annotations.NotNull;


public class TutorialActivity extends BasicActivity {

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.fragment.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ActivityWelcomeBinding mBinding;
    private ImageView[] indicators;

    /**
     * mPage and mSize controls activity (indicators, colors etc) when fragment number controls what we see only
     *
     */
    int mPage = 0;   //  to track page position
    int mSize =5;
    int tutType=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivityWelcomeBinding) dataBinding;

      /*  public static final int TUTORIAL_TYPE_NEW_TRANSACTION = 1;
        public static final int TUTORIAL_TYPE_ADVANCED_TRANSACTIONS = 2;
        public static final int TUTORIAL_TYPE_NEW_VERSION = 3;
        public static final int TUTORIAL_TYPE_TASKS = 4;
        public static final int TUTORIAL_TYPE_DEBT = 5;
        public static final int TUTORIAL_TYPE_BUDGETS = 6;
        public static final int TUTORIAL_TYPE_QUICK_PANEL = 7;*/

        tutType= getIntent().getIntExtra(Constants.KEY_TUTORIAL_TYPE, 0);
        switch (tutType){
            case 1:
                mSize=5;
                break;
            case 2:
                mSize=7;
                break;
            case 3:
                mSize=5;
                break;
            case 4:
                mSize=6;
                break;
            case 5:
                mSize=6;
                break;
            case 6:
                mSize=3;
                break;
            case 7:
                mSize=3;
                break;
            case 8:
                mSize=4;
                break;
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

       setViewPager();

       setClicks();
    }

    @Override
    protected int provideActivityLayout() {
        return R.layout.activity_welcome;
    }

    private void setClicks(){
        mBinding.introBtnNext.setOnClickListener(v -> {
            mPage++;
            mBinding.viewPager.setCurrentItem(mPage, true);
        });

        mBinding.introBtnSkip.setOnClickListener(v ->
               finish());

        mBinding.introBtnFinish.setOnClickListener(v ->
                finish());
    }

    void setViewPager(){
        indicators = new ImageView[]{mBinding.introIndicator0, mBinding.introIndicator1, mBinding.introIndicator2,
                mBinding.introIndicator3, mBinding.introIndicator4, mBinding.introIndicator5, mBinding.introIndicator6,
                mBinding.introIndicator7,  mBinding.introIndicator8,  mBinding.introIndicator9};

        for (int i = 0; i< mSize ; i++)
            indicators[i].setVisibility(View.VISIBLE);

        // Set up the ViewPager with the sections adapter.
        mBinding.viewPager.setAdapter(mSectionsPagerAdapter);

        mBinding.viewPager.setCurrentItem(mPage);
        updateIndicators(mPage);
        mBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mPage = position;

                updateIndicators(mPage);


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
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {


        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given mPage.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return TutorialFragment.newInstance(tutType, position + 1);
        }

        @Override
        public int getCount() {
            return mSize;
        }
    }
}