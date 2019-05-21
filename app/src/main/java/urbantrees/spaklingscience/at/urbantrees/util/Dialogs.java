package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewTreeObserver;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.MainActivity;

/**
 * Created by Laurenz Fiala on 20/09/2017.
 * Provides functionality for showing errors and checking permissions.
 */
public class Dialogs {

    private static Snackbar activeSnackbar;

    /**
     * Shows a non-cancellable dialog with given message, action and positive button text.
     * The negative button closes the app.
     * @param context The calling activity.
     */
    public static void criticalDialog(final Activity context, final int messageStringId, final int positiveBtnStringId, final DialogInterface.OnClickListener positiveAction) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false)
                        .setMessage(messageStringId)
                        .setPositiveButton(positiveBtnStringId, positiveAction)
                        .setNegativeButton(R.string.close_app, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                context.finish();
                            }
                        });
                builder.create().show();
            }
        });
    }

    /**
     * Prompt to show to the user when a critical error occurs.
     * @param context calling activity
     * @param message Message to show to the user.
     */
    public static void errorPrompt(final Activity context, final String message) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false)
                        .setMessage(message)
                        .setNegativeButton(R.string.close_app, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                context.finish();
                            }
                        });
                builder.create().show();
            }
        });
    }

    /**
     * Shows a dialog regarding internet connectivity to the user. The caller can decide what
     * happens when the user wants to retry; if he doesn't, he may close the app.
     * @param context calling activity
     */
    public static void noInternetDialog(final MainActivity context, final DialogInterface.OnClickListener onPositiveClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false)
                .setMessage(R.string.error_no_internet)
                .setPositiveButton(R.string.retry, onPositiveClick)
                .setNegativeButton(R.string.close_app, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.finish();
                    }
                });
        builder.create().show();
    }

    /**
     * Show progress snackbar, or if it already is shown, update its text.
     * @param view The containing view of the calling actiivty.
     * @param text The Snackbar's test to show.
     */
    public static final void progressSnackbar(View view, String text) {

        if (activeSnackbar == null) {
            activeSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
            activeSnackbar.show();
            activeSnackbar.getView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    activeSnackbar.getView().getViewTreeObserver().removeOnPreDrawListener(this);
                    ((CoordinatorLayout.LayoutParams) activeSnackbar.getView().getLayoutParams()).setBehavior(null);
                    return true;
                }
            });
        } else {
            activeSnackbar.setText(text);
        }

    }

    /**
     *
     */
    public static final void dismissSnackbar() {

        if (activeSnackbar != null) {
            activeSnackbar.dismiss();
        }

    }

}
