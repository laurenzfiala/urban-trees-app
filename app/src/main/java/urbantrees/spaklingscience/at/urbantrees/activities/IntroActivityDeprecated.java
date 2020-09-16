package urbantrees.spaklingscience.at.urbantrees.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;

/**
 * The intro screen showing sponsors, app name & icon.
 * Also checks the app permissions and enabled location.
 */
public class IntroActivityDeprecated extends AppCompatActivity {

    /**
     * Period in milliseconds to show the intro screen.
     */
    private static final int INTRO_PERIOD = 1000;

    /**
     * Request code for enabling location.
     */
    public static final int        REQUEST_LOCATION_ENABLE = 3;

    /**
     * Request code for requesting new permissions.
     */
    public static final int        REQUEST_PERMISSIONS = 2;

    /**
     * Permissions needed to be granted for the app to work.
     */
    public static final String[]    NEEDED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * Whether to wait for the permission dialog with continuing to the next activity or not.
     */
    private boolean isShowPermissionDialog = false;

    /**
     * Shows this activity for the duration given in {@link #INTRO_PERIOD}
     * containing title and sponsorships etc.
     * @param savedInstanceState (not used)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_deprecated);

        startIntroTimer();

        requestPermissions();
        if(!this.isShowPermissionDialog) {
            checkLocationEnabled();
        }

    }

    /**
     * Starts timer to show the main activity.
     */
    private void startIntroTimer() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent mainActivityIntent = new Intent(IntroActivityDeprecated.this, MainActivity.class);
                startActivity(mainActivityIntent);

                finish(); // prevent user form returning to title screen

            }

        }, INTRO_PERIOD);

    }

    /**
     * Runtime request for "dangerous" coarse location permission.
     * Only request if not already requested.
     * Only applicable to android 6 and higher (api 23).
     */
    @TargetApi(23)
    private void requestPermissions() {
        if(Build.VERSION.SDK_INT < 23) {
            throw new RuntimeException("Tried to request permissions in android api lower than 23.");
        }

        int granted = 0;
        for (String perm : NEEDED_PERMISSIONS) {
            if (this.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
                granted++;
            }
        }
        if (granted < NEEDED_PERMISSIONS.length) {
            this.isShowPermissionDialog = true;
            Dialogs.dialog(this, R.string.permreq_explanation, R.string.permreq_btn_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    IntroActivityDeprecated.this.requestPermissions(
                            IntroActivityDeprecated.NEEDED_PERMISSIONS,
                            IntroActivityDeprecated.REQUEST_PERMISSIONS
                    );
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int granted = 0;
        if(requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                }
            }
        }

        if(granted < permissions.length) {
            requestPermissions();
        } else {
            checkLocationEnabled();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Check if coarse (network-based) location is enabled.
     */
    private void checkLocationEnabled() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            startIntroTimer();
        } else {
            Dialogs.dialog(this, R.string.enable_location_explanation, R.string.enable, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    IntroActivityDeprecated.this.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION_ENABLE);
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_LOCATION_ENABLE:
                checkLocationEnabled();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
