package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.unexceptional.beast.banko.R;
import com.unexceptional.beast.banko.newVersion.Constants;
import com.unexceptional.beast.banko.newVersion.callback.PickIconCallback;

public class PickIconDialog {

    private AppCompatActivity activity;
    private AlertDialog dialog;
    private PickIconCallback callback;

    public PickIconDialog(AppCompatActivity activity, PickIconCallback callback) {
        this.activity = activity;
        this.callback= callback;
    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_pick_single2, null);
        builder.setView(dialogView);

        //bind views
        RecyclerView recyclerView= dialogView.findViewById(R.id.pick_single);
        Button selectNOne= dialogView.findViewById(R.id.select_none);
        //setRecycler
        ViewAdapter adapter= new ViewAdapter(callback);
        adapter.setIconList(activity.getResources().getStringArray(R.array.ColorIcons));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));

        selectNOne.setOnClickListener(v -> {
            dialog.dismiss();
            showIconPacks();
        });

        //get items
        dialog = builder.create();
        dialog.show();
    }

    private void showIconPacks(){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View dialogView = View.inflate(activity, R.layout.dialog_icon_packs, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

      //  dialogView.findViewById(R.id.btn_first_promo).setOnClickListener(view -> showTutActivity(Constants.TUTORIAL_TYPE_NEW_TRANSACTION));

        dialog.show();
    }


    public void hideDialog(){
        dialog.dismiss();
    }

    private class ViewAdapter extends BasicPickAdapter {

        private String[] iconNamesList;

        @Nullable
        private final PickIconCallback callback;

        ViewAdapter(@Nullable PickIconCallback clickCallback) {
            this.callback = clickCallback;
        }

        void setIconList(String[] iconNamesList) {
            this.iconNamesList= iconNamesList;
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            holder.binding.executePendingBindings();
            String name= iconNamesList[position];

            int id= getDrawable(name);

            if(id!=0)
                Picasso.get().load(id).into(holder.binding.pickIcon);

            if(callback!=null)
                holder.binding.pickIcon.setOnClickListener(v -> {
                    callback.onIconPick(name);
                    dialog.dismiss();
                });
        }

        @Override
        public int getItemCount() {
            return iconNamesList == null ? 0 : iconNamesList.length;
        }

    }
}