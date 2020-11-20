package com.callberry.callingapp.materialdialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.callberry.callingapp.R;

public class MaterialProgress {

    private Activity activity;
    private Dialog dialog;
    private float radius = 4f;
    private int backgroundColor = android.R.color.white;
    private boolean isCancelable = false;

    public MaterialProgress(Activity activity) {
        this.activity = activity;
    }

    public MaterialProgress setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public MaterialProgress setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public MaterialProgress setCancelable(boolean cancelable) {
        isCancelable = cancelable;
        return this;
    }

    public void show() {
        dialog = buildDialog();
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private Dialog buildDialog() {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.dailog_progress, null);
        CardView progressCard = contentView.findViewById(R.id.cardAd);
        progressCard.setRadius(radius);
        progressCard.setCardBackgroundColor(ContextCompat.getColor(activity, backgroundColor));
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(isCancelable);
        dialog.setContentView(contentView);
        return dialog;
    }

}
