package com.javierjordanluque.healthtrackr.util.exceptions;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class ExceptionManager {
    protected static final String ERROR = "E";
    protected static final String WARNING = "W";
    protected static final String INFO = "I";

    protected static void log(String severity, String tag, String exception, String message, String cause) {
        String fullMessage = exception + " | " + message + " caused by " + cause;

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
            View view = ((Activity) context).getWindow().getDecorView().getRootView();

            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
            snackbar.show();
        }
    }
}
