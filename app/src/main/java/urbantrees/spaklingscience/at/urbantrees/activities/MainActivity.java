package urbantrees.spaklingscience.at.urbantrees.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.IOException;
import java.util.Properties;

import urbantrees.spaklingscience.at.urbantrees.BuildConfig;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommandType;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebChromeClient;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebViewClient;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.fragments.StatusBottomSheetFragment;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothCoordinator;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommand;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTManager;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponseType;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
import urbantrees.spaklingscience.at.urbantrees.fragments.DeviceSelectFragment;
import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;
import urbantrees.spaklingscience.at.urbantrees.util.PreferenceManager;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE_ARRAY;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.INTENT_REQUEST_ENABLE_BLUETOOTH;

public class MainActivity extends AppCompatActivity
        implements DeviceSelectFragment.OnDeviceSelectFragmentInteractionListener,
        StatusBottomSheetFragment.OnStatusBottomSheetInteractionListener,
        BluetoothCoordinator.OnBluetoothCoordinatorChange,
        UARTManager.OnUARTManagerStatusChange,
        ApplicationProperties,
        MainActivityInterface {

    private static final String LOGGING_TAG = MainActivity.class.getName();

    // ----- LOGIC -----
    private BluetoothCoordinator bluetoothCoordinator;
    private UARTManager uartManager;
    private HttpManager httpManager;
    private Properties props;
    private PreferenceManager prefManager;

    // ----- WEBVIEW -----
    private CustomWebViewClient webViewClient;
    private CustomWebChromeClient webChromeClient;

    // ----- UI -----
    private WebView webView;
    private FloatingActionButton fab;
    private DeviceSelectFragment deviceSelectFragment;
    private StatusBottomSheetFragment statusFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.loadProperties();
        this.loadPreferences();
        this.initWebView();

        this.httpManager = new HttpManager(this, this);
        this.httpManager.setApiKeyToken(this.getProperty("api.key"));

        this.bluetoothCoordinator = new BluetoothCoordinator(this, this);

        this.fab = findViewById(R.id.fab);
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.showDeviceSelect();
            }
        });

        this.loadInitialPage();

    }

    public void initWebView() {

        this.webViewClient = new CustomWebViewClient(this);
        this.webChromeClient = new CustomWebChromeClient(this);

        this.webView = (WebView) findViewById(R.id.web_view);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        this.webView.setWebViewClient(this.webViewClient);
        this.webView.setWebChromeClient(this.webChromeClient);
        //WebView.setWebContentsDebuggingEnabled(true); // TODO remove

    }

    public void loadInitialPage() {
        this.webView.loadUrl(this.getProperty("initial.load.address"));
    }

    /**
     * Load config file.
     */
    private void loadProperties() {

        this.props = new Properties();

        String propertyFile = "config-prod.properties";
        if (BuildConfig.DEBUG) {
            propertyFile = "config.properties";
        }

        try {
            this.props.load(this.getClass().getResourceAsStream("/assets/" + propertyFile));
        } catch (IOException e) {
            Log.e(LOGGING_TAG, "Could not load config: " + e.getMessage(), e);
        }

    }

    /**
     * Load device stored preferences.onWebviewError
     */
    private void loadPreferences() {
        this.prefManager = new PreferenceManager(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.webView.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {

            case FILECHOOSER_RESULT_CODE:
                this.webChromeClient.onResult(resultCode, intent);
                break;

            case FILECHOOSER_RESULT_CODE_ARRAY:
                this.webChromeClient.onResultArray(resultCode, intent);
                break;

            case INTENT_REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    this.onDeviceSelectOpened();
                } else {
                    this.closeDeviceSelect();
                }
                break;
        }


    }

    @Override
    public void onDeviceSelectOpened() {

        Log.d(MainActivity.LOGGING_TAG, "onFragmentOpened - Start bluetooth scanning");

        this.httpManager.fetchDeviceList(new Callback<Void>() {

            @Override
            public void call(Void v) {

                AsyncTask r = new AsyncTask() {
                    @Override
                    protected Void doInBackground(Object[] objects) {
                        if (MainActivity.this.bluetoothCoordinator.enableBluetooth()) {
                            MainActivity.this.bluetoothCoordinator.scanForDevices(MainActivity.this.httpManager.getAllowedDeviceAddresses());
                        }
                        return null;
                    }
                };
                r.execute();
            }

            @Override
            public void error(Throwable t) {
                Log.e(LOGGING_TAG, "Error fetching device list: " + t.getMessage(), t);
                Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_list_fetch_failed));
            }

        });

    }

    public void showDeviceSelect() {
        Log.d(MainActivity.LOGGING_TAG, "showDeviceSelect - Open device select dialog fragment");

        this.webView.evaluateJavascript(
                "getJWTToken();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        String val = Utils.trimDoubleQuotes(s);
                        if (val == null || "null".equals(val)) {
                            MainActivity.this.getHttpManager().unsetJWTToken();
                        } else {
                            MainActivity.this.getHttpManager().setJWTToken(val);
                        }
                    }
                }
        );

        this.deviceSelectFragment = DeviceSelectFragment.newInstance();
        this.deviceSelectFragment.show(getSupportFragmentManager().beginTransaction(), DeviceSelectFragment.TAG);
    }

    public void closeDeviceSelect() {
        Log.d(MainActivity.LOGGING_TAG, "closeFragment - Close device select dialog fragment");
        this.deviceSelectFragment.dismissAllowingStateLoss();
    }

    @Override
    public void onDeviceSelectClosed() {
        Log.d(MainActivity.LOGGING_TAG, "onFragmentClosed - Stop bluetooth scanning");
        this.bluetoothCoordinator.stopScan();
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device) {

        this.closeDeviceSelect();
        MainActivity.this.fab.setVisibility(View.GONE);

        if (this.uartManager == null) {
            this.uartManager = new UARTManager(this, this.bluetoothCoordinator, this);
        }

        this.statusFragment = StatusBottomSheetFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.status_container, this.statusFragment, StatusBottomSheetFragment.TAG).commit();
        this.statusFragment.setStatus(R.string.comm_device_get_settings);
        this.statusFragment.setProgress(10);

        this.httpManager.getBeaconSettings(device.getBeacon().getId(), new Callback<BeaconSettings>() {
            @Override
            public void call(BeaconSettings beaconSettings) {
                device.getBeacon().setSettings(beaconSettings);
                MainActivity.this.uartManager.start(device);
            }

            @Override
            public void error(Throwable t) {
                Log.e(LOGGING_TAG, t.getMessage(), t);
                Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_settings_fetch_failed));
            }
        });

    }

    @Override
    public void onStatusCancelled() {
        this.uartManager.stop(true);
    }

    @Override
    public void onScanStart() {
        // not in use
    }

    @Override
    public void onBluetoothDeviceDiscovered(BluetoothDevice device) {
        // not in use
    }

    @Override
    public void onNewBluetoothDeviceDiscovered(BluetoothDevice device) {

        for (Beacon b : this.httpManager.getBeacons()) {
            if (b.getBluetoothAddress().equals(device.getAddress())) {
                device.setBeacon(b);
                break;
            }
        }

        if (device.getBeacon() != null) {
            this.deviceSelectFragment.addDevice(device);
        }

    }

    @Override
    public void onDeviceConnecting() {
        this.statusFragment.setStatus(R.string.comm_device_connecting);
        this.statusFragment.setProgress(20);
    }

    @Override
    public void onDeviceConnected() {
        this.statusFragment.setStatus(R.string.comm_device_connected);
        this.statusFragment.setProgress(25);
    }

    @Override
    public void onDeviceDisconnected(boolean isSuccessful) {
        // not used
    }

    @Override
    public void onDeviceCommandExecutionStart(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command) {
        if (cancelled) {
            return;
        }
        this.statusFragment.setStatus(R.string.comm_device_get_data);
    }

    @Override
    public void onDeviceCommandExecutionEnd(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command) {
        if (cancelled) {
            return;
        }
        final int commandExecProgressAmount = 70;
        this.statusFragment.setProgress(
                (int) (this.statusFragment.getProgress() + ((float) commandExecProgressAmount / (float) totalCommandAmount))
        );
    }

    @Override
    public void onDeviceExecuted(final BluetoothDevice device) {

        this.statusFragment.setProgress(90);
        this.statusFragment.setStatus(R.string.comm_device_send_data);

        try {
            UARTLogEntry[] logs = this.uartManager.getSuccessfulCommands().<UARTLogEntry[]>findResponse(UARTResponseType.LOG_ENTRY).getValue();

            this.httpManager.sendBeaconData(device.getBeacon(), logs, new Callback<Void>() {
                @Override
                public void call(Void v) {
                    Log.i(LOGGING_TAG, "Successfully sent beacon data to server");
                    statusFragment.setProgress(95);

                    UARTCommand settingsCmd = uartManager.getSuccessfulCommands().find(UARTCommandType.SETTINGS_COMMAND);
                    UARTCommand telemetricsCmd = uartManager.getSuccessfulCommands().find(UARTCommandType.TELEMETRICS_COMMAND);

                    httpManager.sendBeaconSettings(device.getBeacon(), settingsCmd, telemetricsCmd, new Callback<Void>() {
                        @Override
                        public void call(Void v) {
                            Log.i(LOGGING_TAG, "Successfully sent beacon settings to server");

                            Handler h = new Handler(MainActivity.this.getMainLooper()); // TODO check if needed
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    FragmentManager fm = getSupportFragmentManager();
                                    if (!fm.isDestroyed() && !fm.isStateSaved()) {
                                        getSupportFragmentManager().beginTransaction().remove(statusFragment).commit();
                                    }
                                    redirectAfterBeacon(device);
                                }
                            };
                            h.post(r);


                            getSupportFragmentManager().beginTransaction().remove(statusFragment).commit();
                            redirectAfterBeacon(device);

                        }

                        @Override
                        public void error(Throwable t) {
                            Log.e(LOGGING_TAG, "Failed to send beacon info: " + t.getMessage(), t);
                            Handler h = new Handler(MainActivity.this.getMainLooper()); // TODO check if needed
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_data_send_failed));
                                }
                            };
                            h.post(r);
                        }
                    });

                }

                @Override
                public void error(Throwable t) {
                    Log.e(LOGGING_TAG, "Failed to send beacon info: " + t.getMessage(), t);
                    Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_data_send_failed));
                }
            });
        } catch (Throwable t) {
            Log.e(LOGGING_TAG, "Could not send beacon data: " + t.getMessage());
        }

    }

    @Override
    public void onDeviceCancel(BluetoothDevice device) {
        this.statusFragment.setStatus(R.string.comm_device_cencelling);
        this.statusFragment.setIndeterminate();
    }

    @Override
    public void onDeviceCancelled(BluetoothDevice device) {
        getSupportFragmentManager().beginTransaction()
                .remove(this.statusFragment).commit();

        Handler handler = new Handler(MainActivity.this.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fab.setVisibility(View.VISIBLE);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDeviceExecutionFailed(final BluetoothDevice device) {

        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
        this.uartManager.stop(false);

        Runnable redirectRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.showSearchControls();
                MainActivity.this.webView.loadUrl(
                        MainActivity.this.getProperty(
                                "beacontransfer.load.address.failed",
                                device.getBeacon().getTreeId()
                        )
                );
            }
        };
        mainHandler.post(redirectRunnable);
    }

    private void redirectAfterBeacon(final BluetoothDevice device) {

        Handler mainHandler = new Handler(this.getMainLooper());

        Runnable redirectRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.showSearchControls();
                MainActivity.this.webView.loadUrl(
                        MainActivity.this.getProperty(
                                "beacontransfer.load.address",
                                device.getBeacon().getTreeId(),
                                device.getBeacon().getDeviceId(),
                                MainActivity.this.uartManager.getSuccessfulCommands().<UARTLogEntry[]>findResponse(UARTResponseType.LOG_ENTRY).getValue().length
                        )
                );
            }
        };
        mainHandler.post(redirectRunnable);

    }

    // ----- ApplicationProperties -----
    @Override
    public String getProperty(String propertyKey, Object ...replacements) {
        String property = this.props.getProperty(propertyKey);
        for (Object r : replacements) {
            property = property.replaceFirst("\\{\\}", r.toString());
        }
        return property;
    }

    @Override
    public boolean getBooleanProperty(String propertyKey, Object ...replacements) {
        String property = this.getProperty(propertyKey, replacements);
        return "true".equals(property);
    }

    @Override
    public HttpManager getHttpManager() {
        return this.httpManager;
    }

    // ----- MainActivityInterface -----
    @Override
    public void onWebviewError(WebResourceRequest request, WebResourceError error) {
        if (Build.VERSION.SDK_INT < 21 || request.isForMainFrame()) {
            Dialogs.criticalDialog(this, R.string.webview_resource_failed, R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.loadInitialPage();
                }
            });
        }
    }

    @Override
    public void onWebviewPageFinished() {

    }

    @Override
    public void showSearchControls() {
        if (this.getBooleanProperty("fab.show") && this.prefManager.isTreeDataCollect()) {
            this.fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWebviewResouceLoaded() {
        this.webView.evaluateJavascript(
                "localStorage.setItem('"
                        + this.getProperty("api.key.localStorage.key")
                        + "','"
                        + this.getProperty("api.key")
                        + "');",
                null
        );
    }

}
