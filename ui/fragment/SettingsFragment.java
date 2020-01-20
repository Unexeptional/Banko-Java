package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.databinding.FragmentSettings2Binding;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickBankCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickCategoryCallback;
import com.unexceptional.beast.banko.newVersion.callback.PickProductCallback;
import com.unexceptional.beast.banko.newVersion.db.entity.NotificationEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.SettingsEntity;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.SecondActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.SettingsActivity;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickBankDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickParentCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.PickProductDialog;
import com.unexceptional.beast.banko.newVersion.ui.dialog.categories.PickSubCategoryDialog;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryListViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.CategoryViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.NotificationViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.SettingsViewModel;
import com.unexceptional.beast.banko.other.MyApplication;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends BasicFragment {

    private FragmentSettings2Binding mBinding;
    private NotificationViewModel viewModel;
    //private SettingsViewModel settingsViewModel;
   // private SettingsEntity settingsEntity;
    //private Locale actualLocale;

    private OnFragmentInteractionListener mListener;


    public SettingsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     **/
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(SettingsFragment.this).get(NotificationViewModel.class);
      //  settingsViewModel = ViewModelProviders.of(SettingsFragment.this).get(SettingsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding= DataBindingUtil.inflate(inflater, R.layout.fragment_settings_2, container, false);

        setClicks();
        setVisuals();
        return mBinding.getRoot();
    }

    private void setClicks(){
        mBinding.showBanks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(getString(R.string.key_show_bank_list), isChecked).apply();
        });

        mBinding.showAtStart.setOnClickListener(v -> showOnStartOptions());

       /* mBinding.location.setOnClickListener(v -> showLocationSettings());

        mBinding.language.setOnClickListener(v -> showLanguageOptions());
*/
        mBinding.showWidgetBalance.setOnClickListener(v -> {
            Toast.makeText(getActivity(), getString(R.string.warning_widget_changes), Toast.LENGTH_SHORT).show();
            preferences.edit().putBoolean(getString(R.string.key_show_widget_balance), mBinding.showWidgetBalance.isChecked()).apply();
        });

        mBinding.allowDebts.setOnClickListener(v -> {
            boolean value= mBinding.allowDebts.isChecked();
            preferences.edit().putBoolean(getString(R.string.key_allow_debts), value).apply();
            mBinding.setAreDebtsAllowed(value);

            if(!value) {
                preferences.edit().putBoolean(getString(R.string.key_tr_list_hide_debts), true).apply();
                preferences.edit().putBoolean(getString(R.string.key_hide_debt_in_chart), true).apply();
            }
        });

        mBinding.debtTrList.setOnClickListener(v ->
                preferences.edit().putBoolean(getString(R.string.key_tr_list_hide_debts), mBinding.debtTrList.isChecked()).apply());

        mBinding.debtCharts.setOnClickListener(v ->
                preferences.edit().putBoolean(getString(R.string.key_hide_debt_in_chart), mBinding.debtCharts.isChecked()).apply());

        mBinding.defaultDebtAccount.setOnClickListener(v ->{
            PickProductCallback callback= product -> {
                preferences.edit().putLong(getString(R.string.key_default_debt_account_id_new), product.getId()).apply();
                setDebtAcc(product.getId());
            };
            new PickProductDialog((SettingsActivity)getActivity(), callback, 101L).showDialog();
        });

        mBinding.defaultProduct.setOnClickListener(v ->{
            PickProductCallback callback= product -> {
                preferences.edit().putLong(getString(R.string.key_default_account_id_new), product.getId()).apply();
                setDefaultAccount(product.getId());
            };

            if(preferences.getBoolean(getString(R.string.key_show_bank_list), false)){
                PickBankCallback bankCallback= bank ->
                        new PickProductDialog((SettingsActivity) getActivity(), callback, bank.getId()).showDialog();

                new PickBankDialog((SettingsActivity) getActivity(), bankCallback, true).showDialog();

            } else
                new PickProductDialog((SettingsActivity) getActivity(), callback,0).showDialog();
        });

        mBinding.defaultCategory.setOnClickListener(v ->{
            PickCategoryCallback subCategoryPick= category -> {
                preferences.edit().putLong(getString(R.string.key_default_category_id_new), category.getId()).apply();
                setDefaultCategory(category.getId(), null);
            };

            PickCategoryCallback pickCategoryCallback= category -> {
                preferences.edit().putLong(getString(R.string.key_default_category_id_new), category.getId()).apply();
                setDefaultCategory(category.getId(), null);
                ViewModelProviders.of(SettingsFragment.this).get(CategoryListViewModel.class).getChildCategories(category.getId())
                        .observe(SettingsFragment.this, categoryEntities -> {
                    if(categoryEntities!=null && !categoryEntities.isEmpty()){
                        new PickSubCategoryDialog((SettingsActivity) getActivity(), subCategoryPick, category.getId()).showDialog();
                    }else
                        setDefaultCategory(category.getId(), null);
                });

            };

            new PickParentCategoryDialog((SettingsActivity) getActivity(), pickCategoryCallback, 1).showDialog();
        });

        mBinding.manageCategories.setOnClickListener(v -> {
            Intent intent= new Intent(getActivity(), SecondActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_CATEGORY_LIST_FRAGMENT);
            startActivity(intent);
        });

        mBinding.remindMe.setOnClickListener(v -> {
            if(mBinding.remindMe.isChecked()){
                showTimePicker();
            } else{
                new AppExecutors().diskIO().execute(() -> {
                    NotificationEntity notification= viewModel.getReminderNotification();
                    if(notification!=null){
                        viewModel.delete(notification);
                        setReminder();
                    }
                });
            }
        });

        mBinding.enablePassword.setOnClickListener(v -> mListener.startPassFragment());
    }

    public void setPasswordSwitch(boolean value){
        mBinding.enablePassword.setChecked(value);
    }

    private void setVisuals(){
        setReminder();

        setPasswordSwitch(preferences.getBoolean(getString(R.string.key_enable_pass),false));

        mBinding.showBanks.setChecked(preferences.getBoolean(getString(R.string.key_show_bank_list), false));

        mBinding.showWidgetBalance.setChecked(preferences.getBoolean(getString(R.string.key_show_widget_balance), true));

        mBinding.debtTrList.setChecked(preferences.getBoolean(getString(R.string.key_tr_list_hide_debts), false));

        mBinding.debtCharts.setChecked(preferences.getBoolean(getString(R.string.key_hide_debt_in_chart), true));

        mBinding.setAreDebtsAllowed(preferences.getBoolean(getString(R.string.key_allow_debts), true));

        setDebtAcc(preferences.getLong(getString(R.string.key_default_debt_account_id_new), 0));

        setDefaultAccount(preferences.getLong(getString(R.string.key_default_account_id_new), 0));

        setDefaultCategory(preferences.getLong(getString(R.string.key_default_sub_category_id_new),
                            preferences.getLong(getString(R.string.key_default_category_id_new), 0)), null);

/*
        settingsViewModel.getSettingsLive().observe(this, settingsEntity -> {
            if (settingsEntity!=null){
                actualLocale= new Locale(settingsEntity.getLanguage(), settingsEntity.getLocale());
                if (actualLocale.getCountry()!=null && !actualLocale.getCountry().equals(""))
                    mBinding.location.setText(String.format("%s: %s", getString(R.string.location), actualLocale.getDisplayCountry()));
                else {
                    mBinding.showBanks.setVisibility(View.GONE);
                    preferences.edit().putBoolean(getString(R.string.key_show_bank_list), false).apply();

                    mBinding.location.setText(String.format("%s: %s", getString(R.string.location), getString(R.string.other)));
                    mBinding.language.setText(String.format("%s: %s", getString(R.string.language), actualLocale.getDisplayLanguage()));
                   // mBinding.language.setVisibility(View.VISIBLE);
                }
            }
        });
*/
    }

    private void setDebtAcc(long productId){
        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(
                productId).observe(this, productEntity -> {
            if (productEntity!=null) {

                mBinding.defaultDebtAccount.setText(String.format("%s: %s",
                        getString(R.string.default_debt_account), productEntity.getTitle()));
            } else
                mBinding.defaultDebtAccount.setText(getString(R.string.default_debt_account));
        });
    }

    private void setDefaultAccount(long productId){
        ViewModelProviders.of(this).get(ProductViewModel.class).getProduct(
                productId).observe(this, productEntity -> {
            if (productEntity!=null) {

                mBinding.defaultProduct.setText(String.format("%s: %s",
                        getString(R.string.default_account), productEntity.getTitle()));
            } else
                mBinding.defaultProduct.setText(getString(R.string.default_account));
        });
    }

    private void setDefaultCategory(long categoryId, String subName){
        ViewModelProviders.of(this).get(CategoryViewModel.class).getCategory(categoryId).observe(this, categoryEntity -> {
            if (categoryEntity!=null) {
                if(categoryEntity.getParentId()>0){
                    setDefaultCategory(categoryEntity.getParentId(), categoryEntity.getTitle());
                }else {
                    if(subName!=null)
                        mBinding.defaultCategory.setText(String.format("%s:\n%s (%s)",
                            getString(R.string.default_category), categoryEntity.getTitle(), subName));
                    else
                        mBinding.defaultCategory.setText(String.format("%s:\n%s",
                                getString(R.string.default_category), categoryEntity.getTitle()));
                }
            } else
                mBinding.defaultCategory.setText(getString(R.string.default_category));
        });
    }

    private void showOnStartOptions() {
        final String[] contentOptions = getResources().getStringArray(R.array.ShowAtStartOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(R.string.show_at_start);

        mBuilder.setSingleChoiceItems(contentOptions, preferences.getInt(getString(R.string.key_show_at_start_option), 2),
                (dialogInterface, i) -> {
                    preferences.edit().putInt(getString(R.string.key_show_at_start_option), i).apply();
                    dialogInterface.dismiss();
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

/*
    private void showLocationSettings() {
        final String[] contentOptions = getResources().getStringArray(R.array.LocalizationOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(R.string.show_at_start);

        Locale locale= Locale.getDefault();
        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.LocalizationOptionsDesc));
        int location=0;

        if (strings.contains(locale.toString()))
            location= strings.indexOf(locale.toString());

        mBuilder.setSingleChoiceItems(contentOptions, location,
                (dialogInterface, i) -> {
                    String localeString= strings.get(i);

                    if (localeString.contains("_")){
                        String language= localeString.substring(0, localeString.indexOf("_"));
                        String country= localeString.substring(localeString.indexOf("_") +1);
                        settingsViewModel.updateLocale(country);
                        settingsViewModel.updateLanguage(language);
                        mListener.setLocale(new Locale(language, country));

                    }else{
                        showLanguageOptions();
                    }
                    dialogInterface.dismiss();

                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
*/

/*
    private void showLanguageOptions() {
        final String[] contentOptions = getResources().getStringArray(R.array.LanguageOptions);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        mBuilder.setTitle(R.string.language);

        Locale locale= Locale.getDefault();
        List<String> strings = Arrays.asList(getResources().getStringArray(R.array.LanguageOptionsDesc));
        int language=0;

        if (strings.contains(locale.toString()))
            language= strings.indexOf(locale.toString());

        mBuilder.setSingleChoiceItems(contentOptions, language,
                (dialogInterface, i) -> {
                    String localeString= strings.get(i);
                    settingsViewModel.updateLocale("");
                    settingsViewModel.updateLanguage(localeString);
                    dialogInterface.dismiss();
                    mListener.setLocale(new Locale(localeString));
                });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
*/

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void startPassFragment();
        void setLocale(Locale locale);
    }



    @NotNull
    @Override
    public String toString() {
        return Constants.TAG_SETTINGS_FRAGMENT;
    }

    //REMINDER EVERYDAY
    private void setReminder(){
        new AppExecutors().diskIO().execute(() -> {
            NotificationEntity notification= viewModel.getReminderNotification();
            new AppExecutors().mainThread().execute(() -> {
                if(notification!=null){
                    Calendar calendar= Calendar.getInstance();
                    calendar.setTime(notification.getDate());


                    SimpleDateFormat dateFormatGmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    mBinding.remindMe.setChecked(true);
                    mBinding.remindMe.setText(String.format("%s: %s", getString(R.string.everyday_reminder), dateFormatGmt.format(calendar.getTime())));
                }else {
                    mBinding.remindMe.setChecked(false);
                    mBinding.remindMe.setText( getString(R.string.everyday_reminder));
                }
            });
        });
    }

    private void showTimePicker(){
        final Calendar calendar= Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(getActivity(), (timePicker, selectedHour, selectedMinute) -> {

            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);

            viewModel.insert(new NotificationEntity(getString(R.string.everyday_reminder),
                    getString(R.string.everyday_reminder_summary), calendar.getTime(), Constants.NOTIFICATION_TYPE_REMINDER,
                    "ic_launcher_round", 0));

            setReminder();
        }, hour, minute, true);

        mTimePicker.setTitle(getString(R.string.select_time));
        mTimePicker.show();

    }
}
