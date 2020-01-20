package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.ui.activity.TutorialActivity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;

abstract class BasicListFragment extends BasicFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewModels();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapters();
        reloadItems();
    }

    void setLinearGridLayoutManager(RecyclerView recyclerView, int columnCount){
        int orientation = this.getResources().getConfiguration().orientation;
        int mColumnCount;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            mColumnCount = 0;
        else
            mColumnCount =columnCount;


        if (mColumnCount <= 1)
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        else
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getActivity()), LinearLayoutManager.VERTICAL));
    }

    void setEmptyText(TextView textView, String text, int tutType){
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
        textView.setOnClickListener(v ->{
            Intent intent= new Intent(getActivity(), TutorialActivity.class);
            intent.putExtra(Constants.KEY_TUTORIAL_TYPE, tutType);
            startActivity(intent);
        } );
    }

    void setEmptyText(TextView textView){
        textView.setVisibility(View.GONE);
    }

    abstract void setViewModels();
    abstract void setAdapters();
    public abstract void reloadItems();


}
