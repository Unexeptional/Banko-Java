package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentMoreBinding;
import com.unexceptional.beast.banko.databinding.FragmentSyncBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.other.NotificationPublisher;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SyncFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SyncFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncFragment extends BasicFragment {

    private FragmentSyncBinding mBinding;
    private OnFragmentInteractionListener mListener;
    private NotificationViewModel viewModel;

    public SyncFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static SyncFragment newInstance() {
        SyncFragment fragment = new SyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(SyncFragment.this).get(NotificationViewModel.class);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_sync, container, false);
        mBinding.setCallback(mListener);

        setClicks();

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAccessToken();

        String token = preferences.getString(getString(R.string.key_dropbox_access_token), "");


        if (token !=null && !token.equals("")) {
            mBinding.setIsLoading(true);
            getUser(token);
        } else
            setVisuals(null);

    }

    private void getAccessToken() {
        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            preferences.edit().putString(getString(R.string.key_dropbox_access_token), accessToken).apply();
        }
    }


    private void getUser(String token){
        new AppExecutors().networkIO().execute(()-> {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("Banko").build();
            DbxClientV2 client = new DbxClientV2(config, token);
            FullAccount account= null;

            try {
                account = client.users().getCurrentAccount();

            } catch (DbxException e) {
                e.printStackTrace();
            }

            if (isAdded())
                setVisuals(account);
        });
    }

    private void setVisuals(FullAccount account){

        setAutoBackupVisual();
        setFullSyncVisual();
        setUserVisual(account);
    }

    private void setUserVisual(FullAccount account){
        new AppExecutors().mainThread().execute(() ->{
            if (account!=null){
                mBinding.dropboxSync.setChecked(true);
                mBinding.email.setText(String.format("%s: %s", getString(R.string.logged_in), account.getEmail()));
                mBinding.setIsAuthorized(false);
            }else {
                mBinding.dropboxSync.setChecked(false);
                mBinding.email.setText(getString(R.string.not_logged_in));
                mBinding.setIsAuthorized(true);
            }

            mBinding.setIsLoading(false);
        });

    }



    private void setClicks(){
        mBinding.dropboxSync.setOnClickListener(v ->{
            if (mBinding.dropboxSync.isChecked()){
                Auth.startOAuth2Authentication(Objects.requireNonNull(getActivity()), Constants.KEY_DROPBOX_APP_KEY);
            }else {
                preferences.edit().putString(getString(R.string.key_dropbox_access_token), "").apply();
            }
        });

        mBinding.autoBackup.setOnClickListener(v -> {
            if (mBinding.autoBackup.isChecked())
                setBackupOption();
            else{
                new AppExecutors().diskIO().execute(() -> {
                    NotificationEntity notification= viewModel.getAutoBackupNotification();
                    if(notification!=null){
                        viewModel.delete(notification);
                        preferences.edit().remove(getString(R.string.key_auto_backup_option)).apply();
                        setAutoBackupVisual();
                        deleteSyncNotification();
                    }
                });
            }
        });

        mBinding.fullSync.setOnClickListener(v -> {
            if (mBinding.fullSync.isChecked()) {
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setTitle(getString(R.string.warning_title_full_sync_active))
                        .setPositiveButton(getString(R.string.export_db), (dialog, which) ->{
                            mListener.exportDropbox();
                        })
                        .setNeutralButton(getString(R.string.import_db),(dialog, which) ->{
                            mListener.importDropbox();
                        })
                        .show();

                preferences.edit().putBoolean(getString(R.string.key_full_dropbox_sync), true).apply();

            } else{
                deleteSyncNotification();
            }
        });


    }


    private void deleteSyncNotification(){
        preferences.edit().remove(getString(R.string.key_full_dropbox_sync)).apply();
    }
    //FULL SYNC

    private void setFullSyncVisual(){
        new AppExecutors().mainThread().execute(() -> {
            mBinding.fullSync.setChecked(preferences.getBoolean(getString(R.string.key_full_dropbox_sync), false));
        });
    }


    //AUTO BACKUP

    private void setAutoBackupVisual(){
        new AppExecutors().diskIO().execute(() -> {
            NotificationEntity notificationEntity= viewModel.getAutoBackupNotification();
            new AppExecutors().mainThread().execute(() -> {
                if (notificationEntity!=null){
                    String[] dateOptions = getResources().getStringArray(R.array.AutoBackupOptions);
                    String dateOption= dateOptions[preferences.getInt(getString(R.string.key_auto_backup_option),0)];

                    mBinding.autoBackup.setChecked(true);
                    mBinding.autoBackup.setText(String.format("%s:\n%s", getString(R.string.auto_backup),dateOption));
                }else {
                    mBinding.autoBackup.setChecked(false);
                    mBinding.autoBackup.setText( getString(R.string.auto_backup));
                }
            });
        });
    }

    private void setBackupOption() {
        String[] dateOptions = getResources().getStringArray(R.array.AutoBackupOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(getString(R.string.pick_date));
        mBuilder.setSingleChoiceItems(dateOptions,preferences.getInt(getString(R.string.key_auto_backup_option), 0),
                (dialogInterface, i) -> {

                    preferences.edit().putInt(getString(R.string.key_auto_backup_option), i).apply();
                    setBackupTime();

                    dialogInterface.dismiss();
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }


    private void setBackupTime(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {

            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);

            viewModel.insert(new NotificationEntity(
                    getString(R.string.auto_backup),
                    getString(R.string.auto_backup_execute),
                    calendar.getTime(),
                    Constants.NOTIFICATION_TYPE_AUTO_BACKUP,
                    "ic_launcher_round",
                    0
            ));
            setAutoBackupVisual();

        }, hour, minute, true);

        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();

    }



    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_SYNC_FRAGMENT;
    }

    //Listener stuff

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void exportDB();
        void importDB();
        void importFromCSV();
        void exportDropbox();
        void importDropbox();
    }
}
