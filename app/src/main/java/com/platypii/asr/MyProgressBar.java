package com.platypii.asr;

import android.app.ProgressDialog;
import android.content.Context;

class MyProgressBar {

    private final Context context;
    private ProgressDialog dialog;

    MyProgressBar(Context context) {
        this.context = context;
    }

    void start(String message) {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setMax(1);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        dialog.show();
    }

    void update(String message, int progress, int total) {
        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setIndeterminate(false);
            dialog.setMax(total);
            dialog.setProgress(progress);
            dialog.setMessage(message);
            dialog.show();
        } else {
            dialog.setIndeterminate(false);
            dialog.setMax(total);
            dialog.setProgress(progress);
            dialog.setMessage(message);
        }
    }

    void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}
