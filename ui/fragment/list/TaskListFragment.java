package com.unexceptional.beast.banko.newVersion.ui.fragment.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.database.DatabaseHelper;
import com.unexceptional.beast.banko.databinding.TaskItemBinding;
import com.unexceptional.beast.banko.databinding.TaskListBinding;
import com.unexceptional.beast.banko.database.model.BasicTaskModel;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.DateNavigationCallback;
import com.unexceptional.beast.banko.newVersion.callback.TaskClickCallback;
import com.unexceptional.beast.banko.newVersion.db.converter.DateTypeConverter;
import com.unexceptional.beast.banko.newVersion.db.entity.ParticipationTaskEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.ProductEntity;
import com.unexceptional.beast.banko.newVersion.db.entity.TaskEntity;
import com.unexceptional.beast.banko.newVersion.db.model.Task;
import com.unexceptional.beast.banko.newVersion.other.AppExecutors;
import com.unexceptional.beast.banko.newVersion.ui.activity.FloatingActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.MainActivity;
import com.unexceptional.beast.banko.newVersion.ui.activity.SecondActivity;
import com.unexceptional.beast.banko.newVersion.ui.fragment.dateNavigation.TaskDateNavigationFragment;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ParticipationTaskViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.ProductViewModel;
import com.unexceptional.beast.banko.newVersion.ui.viewModel.TaskListViewModel;
import com.unexceptional.beast.banko.other.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskListFragment extends BasicListFragment {

    private long startDate, endDate, bankId;
    private int dateOption;
    private TaskListBinding mBinding;
    private TaskViewAdapter activeViewAdapter, inactiveViewAdapter;
    private LiveData<List<TaskEntity>> activeLiveData, inactiveLiveData;
    private TaskClickCallback mListener;
    private TaskListViewModel taskListViewModel;
    private ParticipationTaskViewModel participationTaskViewModel;
    private int doneTasks=0, undoneTasks=0;

    private DatabaseHelper databaseHelper;

    private DateNavigationCallback dateNavigationCallback= this::reloadItems;

    @Override
    void setViewModels() {
        taskListViewModel= ViewModelProviders.of(this).get(TaskListViewModel.class);
        participationTaskViewModel= ViewModelProviders.of(this).get(ParticipationTaskViewModel.class);

        databaseHelper= DatabaseHelper.getInstance(getActivity());
    }


    @Override
    void setAdapters(){
        activeViewAdapter = new TaskViewAdapter(mListener);
        mBinding.activeList.setAdapter(activeViewAdapter);

        inactiveViewAdapter = new TaskViewAdapter(mListener);
        mBinding.inactiveList.setAdapter(inactiveViewAdapter);
    }

    @Override
    public void reloadItems(){
        getFilters();

        if(activeLiveData!=null)
            activeLiveData.removeObservers(this);


        if(inactiveLiveData!=null)
            inactiveLiveData.removeObservers(this);

        if(bankId!=0){
            activeLiveData= taskListViewModel.getActiveTasksBank(startDate, endDate, dateOption, bankId);
            inactiveLiveData= taskListViewModel.getDoneTasksBank(startDate, endDate, dateOption, bankId);
        }else {
            activeLiveData= taskListViewModel.getActiveTasks(startDate, endDate, dateOption);
            inactiveLiveData= taskListViewModel.getDoneTasks(startDate, endDate, dateOption);
        }

        activeLiveData.observe(this, taskEntities -> {
            if(taskEntities!=null) {
                undoneTasks= taskEntities.size();
                activeViewAdapter.setTaskList(taskEntities);
            }else
                undoneTasks=0;

            setSummaryBarAndEmptyText();
        });

        inactiveLiveData.observe(this, taskEntities -> {
            if(taskEntities!=null) {
                doneTasks= taskEntities.size();
                inactiveViewAdapter.setTaskList(taskEntities);
            }else
                doneTasks=0;

            setSummaryBarAndEmptyText();
        });

    }
    private void setSummaryBarAndEmptyText(){
        mBinding.summary.setText(String.format(Locale.getDefault(),
                "%s: %s %s: %s", getString(R.string.done), String.valueOf(doneTasks), getString(R.string.undone), String.valueOf(undoneTasks)));

        if (doneTasks==0 && undoneTasks==0)
            setEmptyText(mBinding.emptyListText, getString(R.string.task_list_empty_text), Constants.TUTORIAL_TYPE_TASKS);
        else
            setEmptyText(mBinding.emptyListText);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    public static TaskListFragment newInstance( ) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater, R.layout.task_list,container, false);
        setLinearGridLayoutManager(mBinding.activeList, 2);
        setLinearGridLayoutManager(mBinding.inactiveList, 2);
        setAdapters();

        TaskDateNavigationFragment dateNavigationFragment = (TaskDateNavigationFragment)
                getChildFragmentManager().findFragmentById(R.id.static_date_nav_task_list);
        if(dateNavigationFragment!=null)
        dateNavigationFragment.setHostTaskList(dateNavigationCallback);

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setClicks();
        setVisuals();

        mBinding.inactiveButton.setText(getString(R.string.finished));
        mBinding.activeList.setNestedScrollingEnabled(false);
        mBinding.inactiveList.setNestedScrollingEnabled(false);

        if(databaseHelper.getAllTasks().isEmpty())
            mBinding.showOldTasks.setVisibility(View.GONE);
    }

    private void setClicks(){
        mBinding.newItem.setOnClickListener(v ->{
            Intent intent= new Intent(getActivity(), FloatingActivity.class);
            intent.putExtra(Constants.KEY_FRAGMENT_TAG, Constants.TAG_TASK_FRAGMENT);
            intent.putExtra(Constants.KEY_DATE_OPTION, dateOption);
            startActivity(intent);
        });
        mBinding.inactiveButton.setOnClickListener(v ->
                setFinishedButton(mBinding.inactiveListWrapper.getVisibility()==View.VISIBLE));
        mBinding.showOldTasks.setOnClickListener(v -> showOldList());

        setFilter();
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

    private void getFilters(){
        dateOption= preferences.getInt(getString(R.string.key_task_list_date_option), 0);
        startDate = preferences.getLong(getString(R.string.key_task_list_from_date_millis), 0);
        endDate = preferences.getLong(getString(R.string.key_task_list_to_date_millis), 0);
        bankId = preferences.getLong(getString(R.string.key_task_list_bank_id), 0);

        if (bankId>0)
            mBinding.setIsFilterActive(true);
        else
            mBinding.setIsFilterActive(false);
    }

    private void setFilter(){
        mBinding.getRoot().findViewById(R.id.clear_filter).setOnClickListener(v -> {
            clearFilter();
            reloadItems();
        });
    }

    private void clearFilter(){
        preferences.edit().remove(getString(R.string.key_task_list_bank_id)).apply();
    }

    private void showOldList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View dialogView = View.inflate(getActivity(), R.layout.dialog_pick_single2, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        //setRecycler
        List<BasicTaskModel> tasks= new ArrayList<>();
        tasks.addAll(databaseHelper.getAllTasks());
        tasks.addAll(databaseHelper.getAllBankInfos());

        InfoViewAdapter adapter= new InfoViewAdapter(tasks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //get items
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSnackBar(String text, ParticipationTaskEntity oldParticipation){
        final Snackbar snackbar = Snackbar
                .make(mBinding.getRoot() , text, Snackbar.LENGTH_LONG);

        snackbar.setAction(getString(R.string.undo), v -> {
            oldParticipation.setCompleted(!oldParticipation.isCompleted());
            mListener.updateTask(oldParticipation);
        });

        snackbar.show();
    }


    //listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TaskClickCallback) {
            mListener = (TaskClickCallback) context;
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

    @NonNull
    @Override
    public String toString() {
        return Constants.TAG_TASK_LIST_FRAGMENT;
    }

    public class InfoViewAdapter extends RecyclerView.Adapter<InfoViewAdapter.InfoViewHolder> {

        private List<? extends BasicTaskModel> infoList;

        InfoViewAdapter(List<? extends BasicTaskModel> infoList) {
            this.infoList = infoList;
        }

        @NonNull
        @Override
        public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_info, parent, false);

            return new InfoViewHolder(view); //UWAGA NA NEW!
        }
        @Override
        public void onBindViewHolder(@NonNull final InfoViewHolder holder, int position) {

            final BasicTaskModel bankInfo = infoList.get(position);

            holder.bankinfoNote.setText(bankInfo.getmDescription());
            holder.bankName.setText(databaseHelper.getBank(bankInfo.getmBankId()).getmName());

            holder.setNotification.setVisibility(View.GONE);
            holder.more_info.setOnClickListener(v ->{
                if(bankInfo instanceof com.unexceptional.beast.banko.database.model.Task)
                    databaseHelper.deleteTask(bankInfo.getmId());
                else
                    databaseHelper.deleteBankInfo(bankInfo.getmId());
                infoList.remove(bankInfo);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }



        //VIEW HOLDER
        class InfoViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.info_note)
            EditText bankinfoNote;
            @BindView(R.id.more_info)
            ImageView more_info;
            @BindView(R.id.bank_name)
            TextView bankName;
            @BindView(R.id.set_notification)
            ImageView setNotification;

            InfoViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }


    public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.TaskViewHolder> {

        private List<? extends Task> mTaskList;

        @Override
        public int getItemCount() {
            return mTaskList == null ? 0 : mTaskList.size();
        }

        @Override
        public long getItemId(int position) {
            return mTaskList.get(position).getId();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {

            final TaskItemBinding binding;

            TaskViewHolder(TaskItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        @NotNull
        private final TaskClickCallback mTaskClickCallback;

        TaskViewAdapter(@NotNull TaskClickCallback clickCallback) {
            mTaskClickCallback = clickCallback;
            setHasStableIds(true);
        }

        void setTaskList(final List<? extends Task> taskList) {
            mTaskList = taskList;
            notifyDataSetChanged();

            //bo pokazywało dopiero za drugim razem posrane!
        /*    if (mTaskList == null) {
                mTaskList = taskList;
                notifyItemRangeInserted(0, taskList.size());
            } else {

                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mTaskList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return taskList.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return mTaskList.get(oldItemPosition).getId() ==
                                taskList.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Task newTask = taskList.get(newItemPosition);
                        Task oldTask = mTaskList.get(oldItemPosition);

                        //only stuff that is on card
*//*
                        return newTask.getId() == oldTask.getId()
                                && Objects.equals(newTask.isCompleted(), oldTask.isCompleted())
                                && Objects.equals(newTask.getTitle(), oldTask.getTitle())
                                && Objects.equals(newTask.getTaskType(), oldTask.getTaskType())
                                && Objects.equals(newTask.getDescription(), oldTask.getDescription())
                                && Objects.equals(newTask.getStartDate(), oldTask.getStartDate())
                                && Objects.equals(newTask.getEndDate(), oldTask.getEndDate())
                                && Objects.equals(newTask.getActualAmount(), oldTask.getActualAmount());
*//*

                        return false;
                    }
                });
                mTaskList = taskList;
                result.dispatchUpdatesTo(this);
            }*/
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TaskItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item,
                            parent, false);
            return new TaskViewHolder(binding);
        }

        //HERE THE MAGIC STARTS

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

            TaskItemBinding binding= holder.binding;
            TaskEntity taskEntity= new TaskEntity(mTaskList.get(position));

            new AppExecutors().diskIO().execute(() -> {
                ParticipationTaskEntity particip= participationTaskViewModel.getParticipationRapid(taskEntity.getId(), startDate, endDate);
                if(particip==null)
                    particip= new ParticipationTaskEntity();


                setTaskVisuals(particip, taskEntity, binding);
                particip.setTaskId(taskEntity.getId());
                setTaskClicks(particip, taskEntity, binding);
            });


            if(taskEntity.getDescription()==null || taskEntity.getDescription().equals(""))
                binding.description.setVisibility(View.GONE);
            else
                binding.description.setVisibility(View.VISIBLE);

            binding.setTask(taskEntity);
            binding.executePendingBindings();
            binding.setCallback(mTaskClickCallback);

        }

        private void setTaskClicks(ParticipationTaskEntity particip, TaskEntity taskEntity, TaskItemBinding binding){
            particip.setStartDate(DateTypeConverter.toDate(startDate));
            particip.setEndDate(DateTypeConverter.toDate(endDate));

            new AppExecutors().mainThread().execute(() -> {
                binding.done.setOnClickListener(v ->{
                    if(!particip.isCompleted()){
                        particip.setCompleted(true);
                        mTaskClickCallback.updateTask(particip);
                        binding.setParticipationTask(particip);
                        showSnackBar(getString(R.string.task_done), particip);
                    }
                });

                binding.undone.setOnClickListener(v ->{
                    if(particip.isCompleted()){
                        particip.setCompleted(false);
                        mTaskClickCallback.updateTask(particip);
                        binding.setParticipationTask(particip);
                        showSnackBar(getString(R.string.task_undone), particip);
                    }
                });

                binding.plus.setOnClickListener(v ->{
                    if(particip.getActualAmount() < taskEntity.getAmount()){
                        particip.setActualAmount(particip.getActualAmount()+1);
                        if (taskEntity.getAmount()==particip.getActualAmount())
                            particip.setCompleted(true);
                        mTaskClickCallback.updateTask(particip);
                        binding.setParticipationTask(particip);
                    }
                });

                binding.minus.setOnClickListener(v ->{
                    if(particip.getActualAmount() > 0){
                        particip.setActualAmount(particip.getActualAmount()-1);
                        if (taskEntity.getAmount()>particip.getActualAmount())
                            particip.setCompleted(false);

                        mTaskClickCallback.updateTask(particip);
                        binding.setParticipationTask(particip);
                    }
                });

                binding.showTransactions.setOnClickListener(v ->{
                    Intent intent= new Intent(getActivity(), MainActivity.class);

                    int transactionDateOption;
                    switch (dateOption) {
                        case 0:
                            transactionDateOption=5;
                            break;
                        case 1:
                            if(taskEntity.getMonthLastDay()<31)
                                transactionDateOption = 5;
                            else
                                transactionDateOption = 3;
                            break;
                        case 2:
                            transactionDateOption = 4;
                            break;
                        default:
                            transactionDateOption = 5;
                            break;
                    }

                    preferences.edit()
                            .putInt(getString(R.string.key_temp_select_main_nav), 2)
                            .putLong(getString(R.string.key_tr_list_product_id), taskEntity.getProductId())
                            .putLong(getString(R.string.key_tr_list_bank_id), taskEntity.getBankId())
                            .putInt(getString(R.string.key_tr_list_date_option), transactionDateOption).apply();
                    if (dateOption==0){
                        preferences.edit()
                                .putLong(getString(R.string.key_tr_list_from_date_millis), DateTypeConverter.toLong(taskEntity.getStartDate()))
                                .putLong(getString(R.string.key_tr_list_to_date_millis), DateTypeConverter.toLong(taskEntity.getEndDate())).apply();
                    }else {
                        if (taskEntity.getMonthLastDay()<31) {

                            preferences.edit()
                                    .putLong(getString(R.string.key_tr_list_from_date_millis), startDate)
                                    .putLong(getString(R.string.key_tr_list_to_date_millis), DateTypeConverter.toLong(getMonthLastDate(taskEntity))).apply();
                        } else
                            preferences.edit()
                                    .putLong(getString(R.string.key_tr_list_from_date_millis), startDate)
                                    .putLong(getString(R.string.key_tr_list_to_date_millis), endDate).apply();

                    }


                    startActivity(intent);
                });
            });

        }

        private void setDates(TaskItemBinding binding, TaskEntity taskEntity){
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            if (taskEntity.getStartDate()!=null)
                binding.startDate.setText(dateFormatGmt.format(taskEntity.getStartDate()));

            if (taskEntity.getEndDate()!=null)
                binding.endDate.setText(dateFormatGmt.format(taskEntity.getEndDate()));

        }

        private void setMonthDates(TaskItemBinding binding, TaskEntity taskEntity){
            SimpleDateFormat dateFormatGmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            binding.startDate.setText(dateFormatGmt.format(DateTypeConverter.toDate(startDate)));

            binding.endDate.setText(dateFormatGmt.format(getMonthLastDate(taskEntity)));
        }

        private Date getMonthLastDate(TaskEntity taskEntity){
            Calendar calendar= Calendar.getInstance();
            calendar.setTime(DateTypeConverter.toDate(startDate));
            calendar.add(Calendar.DAY_OF_YEAR, taskEntity.getMonthLastDay()-1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);

            if (calendar.getTime().after(DateTypeConverter.toDate(endDate)))
                return DateTypeConverter.toDate(endDate);
            else
                return calendar.getTime();
        }


        @SuppressLint("ClickableViewAccessibility")
        private void setTaskVisuals(ParticipationTaskEntity particip, TaskEntity taskEntity, TaskItemBinding binding){

            ProductEntity productEntity= ViewModelProviders.of(TaskListFragment.this).get(ProductViewModel.class).getProductRapid(taskEntity.getProductId());
            if (productEntity!=null)
                new AppExecutors().mainThread().execute(() -> {

                    binding.setParticipationTask(particip);
                    binding.productName.setText(productEntity.getTitle());

                    int id= MyApplication.getBankIconId(productEntity.getBankId());
                    if(id!=0)
                        Picasso.get().load(id).into(binding.taskItemBankIcon);

                    binding.actualDoubleAmount.clearFocus();

                    if(taskEntity.getDateOption()==0)
                        setDates(binding, taskEntity);

                    else if (taskEntity.getDateOption()==1){
                        setMonthDates(binding, taskEntity);
                    }else{
                        binding.startDate.setText("");

                        binding.endDate.setText("");
                    }



                    switch (taskEntity.getTaskType()){
                        case Constants.TASK_TYPE_BOOL:
                            break;
                        case Constants.TASK_TYPE_INT:
                            binding.actualIntAmount.setText(String.valueOf(particip.getActualAmount()));
                            break;
                        case Constants.TASK_TYPE_DOUBLE:
                            binding.actualDoubleAmount.setText(String.valueOf(particip.getActualAmount()));
                            binding.actualDoubleAmount.addTextChangedListener(new TextWatcher() {

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    binding.actualDoubleAmount.setCompoundDrawablesWithIntrinsicBounds(0,
                                            0, R.drawable.outline_done_black_24,0);

                                }
                            });

                            binding.actualDoubleAmount.setOnTouchListener((v, event) -> {
                                final int DRAWABLE_RIGHT = 2;

                                if(event.getAction() == MotionEvent.ACTION_UP) {

                                    if(  binding.actualDoubleAmount.getCompoundDrawables()[DRAWABLE_RIGHT]!=null)
                                        if(event.getRawX() >= (  binding.actualDoubleAmount.getRight() - 50 -  binding.actualDoubleAmount.
                                                getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                            double amount;
                                            try {
                                                amount= Double.parseDouble(Objects.requireNonNull(binding.actualDoubleAmount.getText()).toString());
                                            } catch (IllegalArgumentException e) {
                                                e.printStackTrace();
                                                amount=0;
                                            }

                                            particip.setActualAmount(amount);
                                            mTaskClickCallback.updateTask(particip);
                                            binding.setParticipationTask(particip);
                                        }
                                }
                                return false;
                            });
                            break;
                    }
                });
        }
    }

}
