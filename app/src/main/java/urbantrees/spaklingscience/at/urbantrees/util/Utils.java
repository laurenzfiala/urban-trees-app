package urbantrees.spaklingscience.at.urbantrees.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.IntroActivityDeprecated;

/**
 * Created by Laurenz Fiala on 20/09/2017.
 * Contains various utility methods for easy use.
 */
public class Utils {

    /**
     * Checks whether a network connection is available.
     * @return
     */
    public static boolean isNetworkAvailable(final Activity context) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    /**
     * Runtime request for "dangerous" coarse location permission.
     * Only request if not already requested.
     * Only applicable to android 6 and higher (api 23).
     */
    @TargetApi(23)
    public static void requestPermissions(Activity activity, android.support.v4.app.Fragment context, final String[] requestPermissions, final int requestCode) {

        if(Build.VERSION.SDK_INT < 23) {
            throw new RuntimeException("Tried to request permissions in android api lower than 23.");
        }

        if (!Utils.isPermissionsGranted(activity, requestPermissions)) {
            context.requestPermissions(
                    requestPermissions,
                    requestCode
            );
        }

    }

    @TargetApi(23)
    public static boolean isPermissionsGranted(Activity context, final String[] checkPermissions) {
        int granted = 0;
        for (String perm : checkPermissions) {
            if (context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
                granted++;
            }
        }
        if (granted < checkPermissions.length) {
            return false;
        } else {
            return true;
        }
    }

}
