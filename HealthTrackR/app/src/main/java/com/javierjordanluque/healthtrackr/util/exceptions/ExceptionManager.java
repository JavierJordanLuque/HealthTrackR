package com.javierjordanluque.healthtrackr.util.exceptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.javierjordanluque.healthtrackr.R;

public class ExceptionManager {
    protected static final String ERROR = "E";
    protected static final String WARNING = "W";
    protected static final String INFO = "I";

    protected static void log(String severity, String tag, String exception, String message, Throwable cause) {
        String fullMessage;
        if (cause == null) {
            fullMessage = exception + " | " + message;
        } else {
            fullMessage = exception + " | " + message + " caused by " + cause.getClass().getSimpleName();
        }

        if (severity.equals(WARNING)) {
            Log.w(tag, fullMessage);
        } else if (severity.equals(INFO)) {
            Log.i(tag, fullMessage);
        } else {
            Log.e(tag, fullMessage);
        }
    }

    public static void advertiseUI(Context context, String message) {
        if (context instanceof Activity) {
            View rootView = ((Activity) context).findViewById(android.R.id.content);

            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.snackbar_action_more, v -> {
                snackbar.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(message);
                builder.setPositiveButton(R.string.dialog_positive_ok, (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
            snackbar.show();
        }
    }
}
