package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.NotificationListBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.NotificationClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.ui.adapter.NotificationViewAdapter;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationListViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends BasicListFragment {

    private NotificationListBinding mBinding;
    private NotificationViewAdapter viewAdapter;
    private NotificationListViewModel notificationListViewModel;
    private NotificationClickCallback mListener;
    private LiveData<List<NotificationEntity >> liveData;

    @Override
    void setViewModels() {
        notificationListViewModel= ViewModelProviders.of(this).get(NotificationListViewModel.class);
    }
    @Override
    void setAdapters(){
        viewAdapter= new NotificationViewAdapter(mListener);
        mBinding.list.setAdapter(viewAdapter);
    }

    @Override
    public void reloadItems(){
        if(liveData!=null)
            liveData.removeObservers(this);

        liveData= notificationListViewModel.getAllNotifications();

        liveData.observe(this, notificationEntities -> {
            if(notificationEntities!=null){
                List<NotificationEntity> notifications= new ArrayList<>();

                for (NotificationEntity notification: notificationEntities)
                    if(!notification.isDeleted())
                        notifications.add(notification);

                    if (notifications.isEmpty())
                        mBinding.emptyListText.setVisibility(View.VISIBLE);
                    else
                        mBinding.emptyListText.setVisibility(View.GONE);

                viewAdapter.setNotificationList(notifications);
            }
        });

    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationListFragment() {
    }

    public static NotificationListFragment newInstance( ) {
        NotificationListFragment fragment = new NotificationListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_NOTIFICATION_LIST_FRAGMENT;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.notification_list,container, false);
        setLinearGridLayoutManager(mBinding.list, 2);
        setAdapters();

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
        setVisuals();
        reloadItems();
    }

    private void setClicks(){
        mBinding.getRoot().findViewById(R.id.clear_filter).setOnClickListener(v -> {
            //clearFilter();
            reloadItems();
        });

        mBinding.emptyListText.setOnClickListener(v -> {

        });
    }

    private void setVisuals(){

    }




    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NotificationClickCallback) {
            mListener = (NotificationClickCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BankClickCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
