package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentTutorialBinding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.ui.activity.TutorialActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.TutorialActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickSubCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class TutorialFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private FragmentTutorialBinding mBinding;
    private SharedPreferences preferences;


    public TutorialFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TutorialFragment newInstance(int tutType, int sectionNumber) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(Constants.KEY_TUTORIAL_TYPE, tutType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_tutorial, container, false);
        getTutorialType();
        return mBinding.getRoot();
    }

    private void setFragmentSpecifics(String title, final int imageId, String description){
        mBinding.sectionLabel.setText(title);
        Glide
                .with(this)
                .load(imageId)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                .into(mBinding.sectionImg);
        mBinding.sectionDescription.setText(description);
    }


    private void getTutorialType(){
        if (getArguments() != null) {
            switch (getArguments().getInt(Constants.KEY_TUTORIAL_TYPE)){
                case 1:
                    newTransactionTut();
                    break;
                case 2:
                    advancedTransactionTut();
                    break;
                case 3:
                    newVersionTut();
                    break;
                case 4:
                    tasksTut();
                    break;
                case 5:
                    debtTut();
                    break;
                case 6:
                    budgetsTut();
                    break;
                case 7:
                    panelTut();
                    break;
                case 8:
                    widgetTut();
                    break;
            }
        }
    }

    private void newTransactionTut(){
        if (getArguments() != null) {
            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_nt_title_1), R.drawable.tutorial_nt_1,
                            getString(R.string.tutorial_nt_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_nt_title_2), R.drawable.tutorial_nt_2,
                            getString(R.string.tutorial_nt_desc_2));

                    mBinding.sectionDescription.setOnClickListener(v ->{
                        PickProductCallback callback= product -> {
                            preferences.edit().putLong(getString(R.string.key_default_account_id_new), product.getId()).apply();
                        };

                        if(preferences.getBoolean(getString(R.string.key_show_bank_list), false)){
                            PickBankCallback bankCallback= bank ->
                                    new PickProductDialog((TutorialActivity) getActivity(), callback, bank.getId()).showDialog();

                            new PickBankDialog((TutorialActivity) getActivity(), bankCallback, true).showDialog();

                        } else
                            new PickProductDialog((TutorialActivity) getActivity(), callback,0).showDialog();
                    });

                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_nt_title_3), R.drawable.tutorial_nt_3,
                            getString(R.string.tutorial_nt_desc_3));
                    mBinding.sectionDescription.setOnClickListener(v ->{
                        PickCategoryCallback subCategoryPick= category -> {
                            preferences.edit().putLong(getString(R.string.key_default_category_id_new), category.getId()).apply();
                        };

                        PickCategoryCallback pickCategoryCallback= category -> {
                            preferences.edit().putLong(getString(R.string.key_default_category_id_new), category.getId()).apply();
                            ViewModelProviders.of(TutorialFragment.this).get(CategoryListViewModel.class).getChildCategories(category.getId())
                                    .observe(TutorialFragment.this, categoryEntities -> {
                                        if(categoryEntities!=null && !categoryEntities.isEmpty()){
                                            new PickSubCategoryDialog((TutorialActivity) getActivity(), subCategoryPick, category.getId()).showDialog();
                                        }
                                    });

                        };

                        new PickParentCategoryDialog((TutorialActivity) getActivity(), pickCategoryCallback, 1).showDialog();
                    });
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_nt_title_4), R.drawable.tutorial_nt_4,
                            "");
                    break;
                case 5:
                    setFragmentSpecifics(getString(R.string.tutorial_nt_title_5), R.drawable.tutorial_nt_5,
                            "");
                    break;
            }
        }
    }

    private void advancedTransactionTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_1), R.drawable.tutorial_at_1,
                            getString(R.string.tutorial_at_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_2), R.drawable.tutorial_at_2,
                            getString(R.string.tutorial_at_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_3), R.drawable.tutorial_at_3,
                            getString(R.string.tutorial_at_desc_3));
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_4), R.drawable.tutorial_at_4,
                            getString(R.string.tutorial_at_desc_4));
                    break;
                case 5:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_5), R.drawable.tutorial_at_5,
                            getString(R.string.tutorial_at_desc_5));
                    break;
                case 6:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_6), R.drawable.tutorial_at_6,
                            getString(R.string.tutorial_at_desc_6));
                    break;
                case 7:
                    setFragmentSpecifics(getString(R.string.tutorial_at_title_7), R.drawable.tutorial_at_7,
                            getString(R.string.tutorial_at_desc_7));
                    break;
            }
        }
    }

    private void newVersionTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_new_version_title_1), R.drawable.tutorial_new_version_1,
                            getString(R.string.tutorial_new_version_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_new_version_title_2), R.drawable.tutorial_new_version_2,
                            getString(R.string.tutorial_new_version_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_new_version_title_3), R.drawable.tutorial_new_version_3,
                            getString(R.string.tutorial_new_version_desc_3));
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_new_version_title_4), R.drawable.tutorial_new_version_4,
                            getString(R.string.tutorial_new_version_desc_4));
                    break;
                case 5:
                    setFragmentSpecifics(getString(R.string.tutorial_new_version_title_5), R.drawable.tutorial_new_version_5,
                            getString(R.string.tutorial_new_version_desc_5));
                    break;
            }
        }
    }

    private void tasksTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_1), R.drawable.tutorial_task_1,
                            getString(R.string.tutorial_task_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_2), R.drawable.tutorial_task_2,
                            getString(R.string.tutorial_task_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_3), R.drawable.tutorial_task_3,
                            getString(R.string.tutorial_task_desc_3));
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_4), R.drawable.tutorial_task_4,
                            getString(R.string.tutorial_task_desc_4));
                    break;
                case 5:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_5), R.drawable.tutorial_task_5,
                            getString(R.string.tutorial_task_desc_5));
                    break;
                case 6:
                    setFragmentSpecifics(getString(R.string.tutorial_task_title_6), R.drawable.tutorial_task_6,
                            getString(R.string.tutorial_task_desc_6));
                    break;
            }
        }
    }

    private void debtTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_1), R.drawable.tutorial_debt_1,
                            getString(R.string.tutorial_debt_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_2), R.drawable.tutorial_debt_2,
                            getString(R.string.tutorial_debt_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_3), R.drawable.tutorial_debt_3,
                            getString(R.string.tutorial_debt_desc_3));
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_4), R.drawable.tutorial_debt_4,
                            getString(R.string.tutorial_debt_desc_4));
                    break;
                case 5:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_5), R.drawable.tutorial_debt_5,
                            getString(R.string.tutorial_debt_desc_5));
                    break;
                case 6:
                    setFragmentSpecifics(getString(R.string.tutorial_debt_title_6), R.drawable.tutorial_debt_6,
                            getString(R.string.tutorial_debt_desc_6));
                    break;
            }
        }
    }

    private void budgetsTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_budget_title_1), R.drawable.tutorial_budget_1,
                            getString(R.string.tutorial_budget_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_budget_title_2), R.drawable.tutorial_budget_2,
                            getString(R.string.tutorial_budget_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_budget_title_3), R.drawable.tutorial_budget_3,
                            getString(R.string.tutorial_budget_desc_3));
                    break;
            }
        }
    }

    private void panelTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_q_panel_title_1), R.drawable.tutorial_q_panel_1,
                            getString(R.string.tutorial_q_panel_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_q_panel_title_2), R.drawable.tutorial_q_panel_2,
                            getString(R.string.tutorial_q_panel_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_q_panel_title_3), R.drawable.tutorial_q_panel_3,
                            getString(R.string.tutorial_q_panel_desc_3));
                    break;
            }
        }
    }

    private void widgetTut(){
        if (getArguments() != null) {

            switch (getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    setFragmentSpecifics(getString(R.string.tutorial_widget_title_1), R.drawable.tutorial_widget_1,
                            getString(R.string.tutorial_widget_desc_1));
                    break;
                case 2:
                    setFragmentSpecifics(getString(R.string.tutorial_widget_title_2), R.drawable.tutorial_widget_2,
                            getString(R.string.tutorial_widget_desc_2));
                    break;
                case 3:
                    setFragmentSpecifics(getString(R.string.tutorial_widget_title_3), R.drawable.tutorial_widget_3,
                            getString(R.string.tutorial_widget_desc_3));
                    break;
                case 4:
                    setFragmentSpecifics(getString(R.string.tutorial_widget_title_4), R.drawable.tutorial_widget_4,
                            getString(R.string.tutorial_widget_desc_4));
                    break;
            }
        }
    }

}