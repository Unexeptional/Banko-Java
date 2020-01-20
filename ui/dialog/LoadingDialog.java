package com.unexceptional.beast.banko.newVersion.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.unexceptional.beast.banko.R;


public class LoadingDialog {

    private Activity activity;
    private Dialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog(@Nullable String text) {
        if(activity!=null){
            dialog  = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //...set cancelable false so that it's never get hidden
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_loading_layout);
            TextView textView = dialog.findViewById(R.id.loading_msg);
            if(text!=null)
                textView.setText(text);

            dialog.show();
        }
    }


/*
    public void hideDialogFailure(){
        if(activity!=null){
            Toast.makeText(activity, activity.getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
            hideDialog();
        }
    }
*/

    public void hideDialog(){
        if(dialog!=null)
            dialog.dismiss();
    }

}