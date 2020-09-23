package urbantrees.spaklingscience.at.urbantrees.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import urbantrees.spaklingscience.at.urbantrees.BuildConfig;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothCoordinator;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommand;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommandType;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTManager;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponseType;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.bluemaestro.BlueMaestroHelper;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
import urbantrees.spaklingscience.at.urbantrees.entities.Status;
import urbantrees.spaklingscience.at.urbantrees.entities.StatusAction;
import urbantrees.spaklingscience.at.urbantrees.fragments.DeviceSelectFragment;
import urbantrees.spaklingscience.at.urbantrees.fragments.StatusBottomSheetFragment;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebChromeClient;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebViewClient;
import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;
import urbantrees.spaklingscience.at.urbantrees.util.BeaconLogger;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;
import urbantrees.spaklingscience.at.urbantrees.util.PreferenceManager;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE_ARRAY;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.INTENT_LOCATION_SOURCE_SETTINGS;
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

        this.bluetoothCoordinator = new BluetoothCoordinator(
                this,
                this,
                BlueMaestroHelper.getBeaconScanFilters()
        );

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

        this.webViewClient = new CustomWebViewClient(this, this, this);
        this.webChromeClient = new CustomWebChromeClient(this);

        this.webView = (WebView) findViewById(R.id.web_view);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        this.webView.setWebViewClient(this.webViewClient);
        this.webView.setWebChromeClient(this.webChromeClient);

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
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            case FILECHOOSER_RESULT_CODE:
                this.webChromeClient.onResult(resultCode, intent);
                break;

            case FILECHOOSER_RESULT_CODE_ARRAY:
                this.webChromeClient.onResultArray(resultCode, intent);
                break;

            case INTENT_REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    this.showDeviceSelect();
                }
                break;

            case INTENT_LOCATION_SOURCE_SETTINGS:
                this.showDeviceSelect();
                break;
        }


    }

    @Override
    public void onDeviceSelectOpened() {

        Log.d(MainActivity.LOGGING_TAG, "onFragmentOpened - Start bluetooth scanning");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.httpManager.fetchDeviceList(new Callback<Void>() {

                    @Override
                    public void call(Void v) {
                        AsyncTask r = new AsyncTask() {
                            @Override
                            protected Void doInBackground(Object[] objects) {
                                MainActivity.this.bluetoothCoordinator.startScan();
                                return null;
                            }
                        };
                        r.execute();
                    }

                    @Override
                    public void error(Throwable t) {
                        Log.e(LOGGING_TAG, "Error fetching device list: " + t.getMessage(), t);
                        Dialogs.statusDialog(
                                MainActivity.this,
                                new Status(
                                        R.drawable.error_internet_downstream,
                                        R.string.error_beacon_list_fetch_title,
                                        R.string.error_beacon_list_fetch,
                                        new StatusAction() {
                                            @Override
                                            public int getStringResource() {
                                                return R.string.action_cancel;
                                            }

                                            @Override
                                            public void onAction(StatusActivity statusActivity) {
                                                closeDeviceSelect();
                                                statusActivity.finish();
                                            }
                                        },
                                        new StatusAction() {
                                            @Override
                                            public int getStringResource() {
                                                return R.string.retry;
                                            }

                                            @Override
                                            public void onAction(StatusActivity statusActivity) {
                                                MainActivity.this.onDeviceSelectOpened();
                                                statusActivity.finish();
                                            }
                                        }
                                )
                        );
                    }

                });
            }
        };
        Thread t = new Thread(r);
        t.start();

    }

    public void showDeviceSelect() {
        Log.d(MainActivity.LOGGING_TAG, "showDeviceSelect - Open device select dialog fragment");

        if (!(this.bluetoothCoordinator.enableBluetooth() && this.bluetoothCoordinator.enableLocation())) {
            return;
        }

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
    public void onCloseDeviceSelect() {
        Log.d(MainActivity.LOGGING_TAG, "onCloseDeviceSelect - Stop bluetooth scanning");
        this.bluetoothCoordinator.stopScan();
    }

    @Override
    public void onDeviceSelectClosed() {
        Log.d(MainActivity.LOGGING_TAG, "onDeviceSelectClosed");
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device) {

        this.closeDeviceSelect();
        this.fab.setVisibility(View.GONE);

        if (this.uartManager == null) {
            this.uartManager = new UARTManager(this, this, this.bluetoothCoordinator, this);
        }

        this.statusFragment = StatusBottomSheetFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.status_container, this.statusFragment, StatusBottomSheetFragment.TAG).commit();
        this.statusFragment.setStatus(R.string.comm_device_get_settings);
        this.statusFragment.setProgress(10);
        BeaconLogger.debug(device, "Device was selected. Receiving connection parameters.");

        this.httpManager.getBeaconSettings(device.getBeacon().getId(), new Callback<BeaconSettings>() {
            @Override
            public void call(BeaconSettings beaconSettings) {
                device.getBeacon().setSettings(beaconSettings);
                MainActivity.this.uartManager.start(device);
            }

            @Override
            public void error(Throwable t) {
                Log.e(LOGGING_TAG, t.getMessage(), t);
                Dialogs.statusDialog(
                        MainActivity.this,
                        new Status(
                                R.drawable.error_internet_downstream,
                                R.string.error_beacon_settings_fetch_title,
                                R.string.error_beacon_settings_fetch,
                                new StatusAction() {
                                    @Override
                                    public int getStringResource() {
                                        return R.string.retry;
                                    }

                                    @Override
                                    public void onAction(StatusActivity statusActivity) {
                                        MainActivity.this.onDeviceSelected(device);
                                        statusActivity.finish();
                                    }
                                }
                        )
                );
            }
        });

    }

    @Override
    public void onStatusCancelled() {
        this.uartManager.stop(true);
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Cancelling beacon connection...");
        BeaconLogger.send(this);
    }

    @Override
    public void onScanStart() {
        // not in use
    }

    @Override
    public void onScanFailed() {

        Log.e(LOGGING_TAG, "Failed to start BLE scan.");
        Dialogs.statusDialog(
                this,
                new Status(
                        R.drawable.error_general,
                        R.string.error_beacon_scan_title,
                        R.string.error_beacon_scan,
                        new StatusAction() {
                            @Override
                            public int getStringResource() {
                                return R.string.action_cancel;
                            }

                            @Override
                            public void onAction(StatusActivity statusActivity) {
                                statusActivity.finish();
                            }
                        },
                        new StatusAction() {
                            @Override
                            public int getStringResource() {
                                return R.string.retry;
                            }

                            @Override
                            public void onAction(StatusActivity statusActivity) {
                                statusActivity.finish();
                                MainActivity.this.showDeviceSelect();
                            }
                        }
                )
        );

    }

    @Override
    public void onBluetoothDeviceDiscovered(BluetoothDevice device) {

        if (this.attachBeaconToDevice(device)) {
            Log.d(LOGGING_TAG, "Re-discovered known beacon: " + device);
            BeaconLogger.trace(device, "Re-discovered known beacon: " + device);
        }

        this.deviceSelectFragment.updateDevice(device);

    }

    @Override
    public void onNewBluetoothDeviceDiscovered(BluetoothDevice device) {

        if (this.attachBeaconToDevice(device)) {
            Log.d(LOGGING_TAG, "Discovered known beacon: " + device);
            BeaconLogger.trace(device, "Discovered known beacon: " + device);
        }

        this.deviceSelectFragment.addDevice(device);

    }

    private boolean attachBeaconToDevice(final BluetoothDevice device) {

        if (this.httpManager.getAllowedDeviceAddresses().contains(device.getAddress())) {
            for (Beacon b : this.httpManager.getBeacons()) {
                if (b.getBluetoothAddress().equals(device.getAddress())) {
                    device.setBeacon(b);
                    return true;
                }
            }
        }

        return false;

    }

    @Override
    public void onDeviceConnecting() {
        this.statusFragment.setStatus(R.string.comm_device_connecting);
        this.statusFragment.setProgress(20);
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Connecting to beacon.");
    }

    @Override
    public void onDeviceConnected() {
        this.statusFragment.setStatus(R.string.comm_device_connected);
        this.statusFragment.setProgress(25);
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Successfully connected to beacon.");
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
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Receiving data from beacon. Executing command: " + command);
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
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Received data from beacon. Executed command: " + command);
    }

    @Override
    public void onDeviceExecuted(final BluetoothDevice device) {

        this.statusFragment.setProgress(90);
        this.statusFragment.setStatus(R.string.comm_device_send_data);
        BeaconLogger.trace(device, "Data successfully received. Sending to backend.");

        try {
            UARTLogEntry[] logs = this.uartManager.getSuccessfulCommands().<UARTLogEntry[]>findResponse(UARTResponseType.LOG_ENTRY).getValue();
            UARTCommand settingsCmd = uartManager.getSuccessfulCommands().find(UARTCommandType.SETTINGS_COMMAND);
            UARTCommand telemetricsCmd = uartManager.getSuccessfulCommands().find(UARTCommandType.TELEMETRICS_COMMAND);

            this.sendBeaconData(device, logs, settingsCmd, telemetricsCmd);
        } catch (Throwable t) {
            Log.e(LOGGING_TAG, "Could not send beacon data: " + t.getMessage());
            BeaconLogger.error(device, "Data could not be sent to backend: " + t.getMessage());
        }

    }

    private void sendBeaconData(final BluetoothDevice device,
                                final UARTLogEntry[] logs,
                                final UARTCommand settingsCmd,
                                final UARTCommand telemetricsCmd) {

        final Callback<Void> callback = new Callback<Void>() {
            @Override
            public void call(Void v) {
                Log.i(LOGGING_TAG, "Successfully sent beacon readout result to server");
                BeaconLogger.debug(device, "Data successfully sent to backend.");
                statusFragment.setProgress(95);

                Handler h = new Handler(MainActivity.this.getMainLooper());
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
                BeaconLogger.error(device, "Data could not be sent to backend: " + t.getMessage());
                Dialogs.statusDialog(
                        MainActivity.this,
                        new Status(
                                R.drawable.error_internet_upstream,
                                R.string.error_beacon_data_send_title,
                                R.string.error_beacon_data_send,
                                new StatusAction() {
                                    @Override
                                    public int getStringResource() {
                                        return R.string.action_cancel;
                                    }

                                    @Override
                                    public void onAction(StatusActivity statusActivity) {
                                        statusActivity.finish();
                                    }
                                },
                                new StatusAction() {
                                    @Override
                                    public int getStringResource() {
                                        return R.string.retry;
                                    }

                                    @Override
                                    public void onAction(StatusActivity statusActivity) {
                                        MainActivity.this.sendBeaconData(device, logs, settingsCmd, telemetricsCmd);
                                        statusActivity.finish();
                                    }
                                }
                        )
                );
            }
        };

        this.httpManager.sendBeaconResult(device, logs, settingsCmd, telemetricsCmd, callback);

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

        BeaconLogger.send(this);

        this.uartManager.stop(false);

        Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
        Runnable redirectRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.showSearchControls();
                MainActivity.this.webView.loadUrl(
                        MainActivity.this.getProperty(
                                "beacontransfer.load.address.failed",
                                device.getBeacon().getId()
                        )
                );
            }
        };
        mainHandler.post(redirectRunnable);
    }

    private void redirectAfterBeacon(final BluetoothDevice device) {

        BeaconLogger.send(this);

        Handler mainHandler = new Handler(this.getMainLooper());
        Runnable redirectRunnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.this.showSearchControls();
                final String url = MainActivity.this.getProperty(
                        "beacontransfer.load.address",
                        device.getBeacon().getId(),
                        MainActivity.this.uartManager.getSuccessfulCommands().<UARTLogEntry[]>findResponse(UARTResponseType.LOG_ENTRY).getValue().length
                );
                Log.d(LOGGING_TAG, "Loading success page: " + url);
                MainActivity.this.webView.loadUrl(url);
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
    public String[] getArrayProperty(String propertyKey, Object ...replacements) {
        String property = this.props.getProperty(propertyKey);
        for (Object r : replacements) {
            property = property.replaceFirst("\\{\\}", r.toString());
        }
        return property.split("\\s");
    }

    @Override
    public boolean getBooleanProperty(String propertyKey, Object ...replacements) {
        String property = this.getProperty(propertyKey, replacements);
        return "true".equals(property);
    }

    public HttpManager getHttpManager() {
        return this.httpManager;
    }

    // ----- MainActivityInterface -----
    @Override
    public void onWebviewError(WebResourceRequest request, WebResourceError error) {
        if (request.isForMainFrame()) {
            Dialogs.statusDialog(
                    MainActivity.this,
                    new Status(
                            R.drawable.error_internet_upstream,
                            R.string.error_connection_failed_title,
                            R.string.error_connection_failed,
                            new StatusAction() {
                                @Override
                                public int getStringResource() {
                                    return R.string.retry;
                                }

                                @Override
                                public void onAction(StatusActivity statusActivity) {
                                    MainActivity.this.loadInitialPage();
                                    statusActivity.finish();
                                }
                            }
                    )
            );
        }
    }

    @Override
    public void onWebviewPageFinished() {
        this.webView.evaluateJavascript(
                "localStorage.setItem('"
                        + this.getProperty("api.key.localStorage.key")
                        + "','"
                        + this.getProperty("api.key")
                        + "'); refreshLogin();",
                null
        );
    }

    @Override
    public void showSearchControls() {
        if (this.getBooleanProperty("fab.show") && this.prefManager.isTreeDataCollect()) {
            this.fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWebviewResouceLoaded() {
    }

}
