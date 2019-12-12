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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.IntroActivityDeprecated;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHeader;

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

    public static HttpHeader[] mapToHttpHeaders(Map<String, String> map) {
        HttpHeader[] headers = new HttpHeader[map.size()];
        int i = 0;
        for (Map.Entry<String, String> h : map.entrySet()) {
            headers[i] = new HttpHeader(h.getKey(), h.getValue());
            i++;
        }
        return headers;
    }

    public static String trimDoubleQuotes(String toBeTrimmed) {
        if (toBeTrimmed == null) {
            return null;
        }

        int start = 0;
        int end = toBeTrimmed.length();
        for (int i = 0; i < toBeTrimmed.length(); i++) {
            if (toBeTrimmed.charAt(i) == '"' && start == i) {
                start++;
            }
            if (toBeTrimmed.charAt(i) == '"' && end == toBeTrimmed.length() - i) {
                end--;
            }
        }
        return toBeTrimmed.substring(start, end);
    }

    /**
     * Parses the reference date from the given devices' advertisement package.
     * @param device bluetooth device
     * @return the set reference date or null if the date is unset
     * @throws RuntimeException if adv pkg is null
     */
    public static Date geAdvPkgRefDate(final BluetoothDevice device) {

        if (device.getAdvertisementPkg() == null) {
            throw new RuntimeException("Advertisement pkg not set in BluetoothDevice " + device);
        }

        byte[] trimmedAdvPkg = ByteUtils.trim(device.getAdvertisementPkg());
        int trimmedAdvPkgLength = trimmedAdvPkg.length;
        long rawDateNum = ByteUtils.octalToDecimal(Arrays.copyOfRange(trimmedAdvPkg, trimmedAdvPkgLength-4, trimmedAdvPkgLength));
        if (rawDateNum == 0) {
            return null;
        }

        String rawDate = String.valueOf(rawDateNum);
        DateFormat df = new SimpleDateFormat("yyMMddhhmm");
        try {
            return df.parse(rawDate);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse ref date from advertisement pkg.");
        }

    }

    /**
     * Correct RH over 100% and correct dew point.
     * @param logEntry log entry to correct, this instance is not modified
     * @return if the humidity did not need correction, return logEntry; otherwise a new instance
     *         with the correct values.
     */
    public static UARTLogEntry correctHumidity(UARTLogEntry logEntry) {

        if (logEntry.getHumidity() <= 100) {
            return logEntry;
        }

        return new UARTLogEntry(
                logEntry.getObservationDate(),
                logEntry.getTemperature(),
                100,
                logEntry.getTemperature() // when humidity is 100, dew point = temperature
        );

    }

}
