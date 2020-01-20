package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.ComplexListBinding;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * A abstract fragment representing a list of Items.
 * When in portrait orientation - linear layout manager.
 * When in landscape orientation - has a grid layout with 3 columns
 * additionally having summary and filter and inacive items
 * <p/>
 */
public abstract class ComplexListFragment extends BasicListFragment {

    protected ComplexListBinding mBinding;
    boolean isFilterActive;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComplexListFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.complex_list,container, false);
        setLinearGridLayoutManager(mBinding.activeList, 3);
        setLinearGridLayoutManager(mBinding.inactiveList, 3);
        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
        setVisuals();

        mBinding.inactiveButton.setText(setButtonText());
        mBinding.activeList.setNestedScrollingEnabled(false);
        mBinding.inactiveList.setNestedScrollingEnabled(false);
        setObserver();

        getBundle();
    }

    private void setClicks(){
        mBinding.inactiveButton.setOnClickListener(v ->
                setFinishedButton(mBinding.inactiveListWrapper.getVisibility()==View.VISIBLE));
    }

    private void setFinishedButton(boolean value){
        if(value){
            mBinding.inactiveListWrapper.setVisibility(View.GONE);
            mBinding.inactiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0, R.drawable.baseline_expand_less_white_24,0);
        }else{
            mBinding.inactiveListWrapper.setVisibility(View.VISIBLE);
            mBinding.inactiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.baseline_expand_more_white_24,0);
        }

        preferences.edit().putBoolean(getString(R.string.key_hide_) + getClass().getName() , value).apply();
    }

    private void setVisuals(){
        setFinishedButton(preferences.getBoolean(getString(R.string.key_hide_) + getClass().getName(), false));
    }

    //kids follow that
    protected abstract void setObserver();
    protected abstract void getBundle();
    protected abstract void clearFilter();
    protected void setEmptyText(boolean value){
        if (value)
            mBinding.emptyListText.setVisibility(View.VISIBLE);
        else
            mBinding.emptyListText.setVisibility(View.GONE);
    }
    protected abstract String setButtonText();

}
