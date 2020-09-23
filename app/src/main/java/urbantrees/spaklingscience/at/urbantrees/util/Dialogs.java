package urbantrees.spaklingscience.at.urbantrees.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.ViewTreeObserver;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.StatusActivity;
import urbantrees.spaklingscience.at.urbantrees.entities.Status;

/**
 * Created by Laurenz Fiala on 20/09/2017.
 * Provides functionality for showing errors and checking permissions.
 */
public class Dialogs {

    private static Snackbar activeSnackbar;

    /**
     * Theme to use for all dialogs created with this class.
     */
    private static int THEME = R.style.dialog;

    /**
     * Shows a non-cancellable dialog with given message, action and positive button text.
     * The negative button closes the app.
     * @param context The calling activity.
     */
    public static void dialog(final Activity context,
                              final int messageStringId,
                              final int positiveBtnStringId,
                              final DialogInterface.OnClickListener positiveAction) {
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(context, THEME)
                );
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
     * Shows a cancellable dialog with given message, action and positive button text.
     * The cancel button/outside click triggers the cancelAction event.
     * @param context The calling activity.
     * @param layoutResId Resource id of the layout to be displayed as the dialog content.
     * @param positiveBtnStringId String id of the positive btn text to show.
     * @param positiveAction listener called when the positive action is clicked by the user.
     * @param cancelAction listener called when the dialog is cancelled by the user.
     */
    public static void dialog(final Activity context,
                              final int layoutResId,
                              final int positiveBtnStringId,
                              final DialogInterface.OnClickListener positiveAction,
                              final DialogInterface.OnCancelListener cancelAction) {
        Handler h = new Handler(context.getMainLooper());

        final View contentView = context.getLayoutInflater().inflate(R.layout.view_dialog_enable_location, null);

        h.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(context, THEME)
                );
                builder.setView(contentView)
                        .setPositiveButton(positiveBtnStringId, positiveAction)
                        .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelAction.onCancel(dialog);
                            }
                        })
                        .setOnCancelListener(cancelAction);
                builder.create().show();
            }
        });
    }

    /**
     * Opens the status activity showing the given status information.
     * @param context The calling activity.
     * @param status The status and actions to display.
     */
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

    public static void dismissSnackbar() {

        if (activeSnackbar != null) {
            activeSnackbar.dismiss();
        }

    }

}
