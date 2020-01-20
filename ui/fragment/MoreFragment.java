package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentMoreBinding;
import com.unexceptional.beast.banko.newVersion.Constants;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends BasicFragment {

    private FragmentMoreBinding mBinding;

    private OnFragmentInteractionListener mListener;

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_more, container, false);

        mBinding.setCallback(mListener);

        if(getResources().getConfiguration().locale.getCountry().equals("PL")){
            mBinding.fragmentMoreBankPromos.setVisibility(View.VISIBLE);
        }else
            mBinding.fragmentMoreBankPromos.setVisibility(View.GONE);

        long adsStoppedTime= preferences.getLong(getString(R.string.key_ads_disabled_timeout),0L);

        if (adsStoppedTime > Calendar.getInstance().getTimeInMillis()){
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Calendar calendar= Calendar.getInstance();
            calendar.setTimeInMillis(adsStoppedTime);
            mBinding.fragmentMoreStopAds.setText(String.format("%s: %s", getString(R.string.ads_stopped_until), dateFormatGmt.format(calendar.getTime())));
        }else
            mBinding.fragmentMoreStopAds.setText(R.string.stop_ads);

        return mBinding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();

        if(preferences.getBoolean(getString(R.string.key_allow_debts), true))
            mBinding.fragmentMoreDebts.setVisibility(View.VISIBLE);
        else
            mBinding.fragmentMoreDebts.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_MORE_FRAGMENT;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void startSettingsFragment();
        void startTasksFragment();
        void startNotificationsFragment();
        void startExchangeFragment();
        void startDebtsFragment();
        void showBankPromos();
        void stopAds();
        void startLoginFragment();
        void startRegisterFragment();
        void startAccountFragment();
        void startTutorialsFragment();
        void startAboutFragment();
        void rateThisApp();
        void sendFeedback();
        void backupOptions();
        void privacyPolicy();
    }
}
