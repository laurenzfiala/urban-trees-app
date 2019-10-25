package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode;
import urbantrees.spaklingscience.at.urbantrees.activities.IntroActivity;
import urbantrees.spaklingscience.at.urbantrees.activities.MainActivity;
import urbantrees.spaklingscience.at.urbantrees.activities.StatusActivity;
import urbantrees.spaklingscience.at.urbantrees.entities.Status;
import urbantrees.spaklingscience.at.urbantrees.entities.StatusAction;

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
    public static void dialog(final Activity context, final int messageStringId, final int positiveBtnStringId, final DialogInterface.OnClickListener positiveAction) {
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

    // TODO
    public static void statusDialog(final Activity context, final Status status) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {

                StatusActivity.putStatus(status);
                context.startActivity(new Intent(context, StatusActivity.class));

            }
        });
    }

    /**
     * Show progress snackbar, or if it already is shown, update its text.
     * @param view The containing view of the calling actiivty.
     * @param text The Snackbar's test to show.
     */
    public static void progressSnackbar(View view, String text) {

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

    // TODO
    public static void dismissSnackbar() {

        if (activeSnackbar != null) {
            activeSnackbar.dismiss();
        }

    }

}
