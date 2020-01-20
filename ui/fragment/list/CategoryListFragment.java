package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.CategoryListBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.BankClickCallback;
import com.unexceptional.beast.banko.newVersion.callback.CategoryClickCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.CategoryEntity;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.SecondActivity;
import com.unexceptional.beast.banko.newVersion.ui.adapter.ExpandableCategoryViewAdapter;
import com.unexceptional.beast.banko.newVersion.ui.fragment.BasicFragment;
import com.unexceptional.beast.banko.newVersion.ui.fragment.entityBased.BudgetFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link BankClickCallback}
 * interface.
 */
public class CategoryListFragment extends BasicListFragment {

    private CategoryListBinding mBinding;
    private ExpandableCategoryViewAdapter activeViewAdapter;
    private CategoryClickCallback mListener;
    private  CategoryListViewModel viewModel;
    private Observer<List<CategoryEntity>> observer;


    @Override
    void setViewModels() {
        viewModel = ViewModelProviders.of(this).get(CategoryListViewModel.class);
    }

    @Override
    void setAdapters() {
        // Setup expandable feature and RecyclerView
        RecyclerViewExpandableItemManager expMgr = new RecyclerViewExpandableItemManager(null);
        mBinding.activeList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.activeList.setAdapter(expMgr.createWrappedAdapter(activeViewAdapter = new ExpandableCategoryViewAdapter(mListener)));

        // NOTE: need to disable change animations to ripple effect work properly
        ((SimpleItemAnimator) Objects.requireNonNull(mBinding.activeList.getItemAnimator())).setSupportsChangeAnimations(false);
        expMgr.attachRecyclerView(mBinding.activeList);
    }

    @Override
    public void reloadItems() {
        viewModel.getAllCategories().observe(this, observer);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategoryListFragment() {
    }

    public static CategoryListFragment newInstance( ) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        observer= categoryEntities -> {
            if (categoryEntities != null)
                activeViewAdapter.setParentCategories(doParents(categoryEntities));

            mBinding.executePendingBindings();
        };
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.category_list,container, false);
        setClicks();
        setAdapters();

        reloadItems();
        return mBinding.getRoot();
    }

    private List<ParentCategory> doParents(List<CategoryEntity> categoryEntities){

        List<ParentCategory> active= new ArrayList<>();
        LongSparseArray<List<CategoryEntity>> children= new LongSparseArray<>();

        for(CategoryEntity category: categoryEntities) {
            long parentId= category.getParentId();

            if (parentId>0)
                if(children.containsKey(parentId)) {
                    List<CategoryEntity> kids= children.get(parentId);
                    if(kids!=null)
                        kids.add(category);
                    children.append(parentId, kids);
                } else {
                    List<CategoryEntity> kids= new ArrayList<>();
                    kids.add(category);
                    children.append(parentId, kids);
                }
        }

        for(CategoryEntity category: categoryEntities) {
            if (category.getParentId()==0 && category.getId()!=1)//DISABLE DEBTS HERE AS WELL (cant manage debt)
                if(children.get(category.getId())!=null)
                    active.add(new ParentCategory(category, children.get(category.getId())));
                else
                    active.add(new ParentCategory(category, new ArrayList<>()));
        }
        return active;
    }

    private void setClicks() {
        mBinding.newCat.setOnClickListener(v -> {
            Intent intent= new Intent(getActivity(), FloatingActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_CATEGORY_FRAGMENT);
            intent.putExtra(Constants.KEY_CATEGORY_SUB_MODE, false);
            startActivity(intent);
        });

        mBinding.newSubcat.setOnClickListener(v -> {
            Intent intent= new Intent(getActivity(), FloatingActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_CATEGORY_FRAGMENT);
            intent.putExtra(Constants.KEY_CATEGORY_SUB_MODE, true);

            startActivity(intent);
        });
    }


    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_CATEGORY_LIST_FRAGMENT;
    }

    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CategoryClickCallback) {
            mListener = (CategoryClickCallback) context;
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

    public class ParentCategory extends CategoryEntity {

        public ParentCategory(CategoryEntity categoryEntity, List<CategoryEntity> kids) {
            super(categoryEntity.getId(), categoryEntity.getParentId(), categoryEntity.getTitle(), categoryEntity.getIconName(), categoryEntity.isInactive(),
                    categoryEntity.getOrder(), categoryEntity.getType());
            children= kids;
        }

        private List<CategoryEntity> children;

        public List<CategoryEntity> getChildren() {
            return children;
        }

        public void setChildren(List<CategoryEntity> children) {
            this.children = children;
        }
    }
}
