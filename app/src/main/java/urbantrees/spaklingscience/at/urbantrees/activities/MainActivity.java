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
import java.io.InputStream;
import java.util.Properties;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import urbantrees.spaklingscience.at.urbantrees.entities.TransferState;
import urbantrees.spaklingscience.at.urbantrees.entities.TransferStatus;
import urbantrees.spaklingscience.at.urbantrees.fragments.DeviceSelectFragment;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebChromeClient;
import urbantrees.spaklingscience.at.urbantrees.http.CustomWebViewClient;
import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;
import urbantrees.spaklingscience.at.urbantrees.util.BeaconLogger;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;
import urbantrees.spaklingscience.at.urbantrees.util.PreferenceManager;
import urbantrees.spaklingscience.at.urbantrees.util.TransferJSInterface;
import urbantrees.spaklingscience.at.urbantrees.util.TransferJSListener;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE_ARRAY;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.INTENT_LOCATION_SOURCE_SETTINGS;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.INTENT_REQUEST_ENABLE_BLUETOOTH;

public class MainActivity extends AppCompatActivity
        implements DeviceSelectFragment.OnDeviceSelectFragmentInteractionListener,
        TransferJSListener,
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
    private BluetoothDevice selectedDevice;
    private Stack<TransferState> states;
    private boolean isApiKeyStored = false;

    // ----- WEBVIEW -----
    private CustomWebViewClient webViewClient;
    private CustomWebChromeClient webChromeClient;

    // ----- UI -----
    private WebView webView;
    private FloatingActionButton fab;

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

        this.webView.addJavascriptInterface(new TransferJSInterface(this), TransferJSInterface.name());

    }

    public void loadInitialPage() {
        this.webView.loadUrl(this.getProperty("initial.load.address"));
    }

    /**
     * Load config file.
     */
    private void loadProperties() {

        this.props = new Properties();

        String propertyFile = "config-prod";
        if (BuildConfig.DEBUG) {
            propertyFile = "config";
        }

        try (InputStream is1 = this.getPropertiesFile(propertyFile);
             InputStream is2 = this.getPropertiesFile(propertyFile + "-confidential")) {
            this.props.load(is1);
            this.props.load(is2);
        } catch (IOException e) {
            Log.e(LOGGING_TAG, "Could not load config: " + e.getMessage(), e);
        }

    }

    private InputStream getPropertiesFile(String filename) {
        return this.getClass().getResourceAsStream("/assets/" + filename + ".properties");
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
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
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
        this.getDeviceSelectFragment().onLoadDeviceList();

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
                        Handler h = new Handler(getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                getDeviceSelectFragment().onSearchDevices();
                            }
                        });
                        r.execute();
                    }

                    @Override
                    public void error(Throwable t) {
                        Log.e(LOGGING_TAG, "Error fetching device list: " + t.getMessage(), t);
                        Dialogs.statusDialog(
                                MainActivity.this,
                                new Status(
                                        R.drawable.error_internet_downstream,
                                        getString(R.string.error_beacon_list_fetch_title),
                                        getString(R.string.error_beacon_list_fetch),
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

        DeviceSelectFragment
                .newInstance()
                .show(getSupportFragmentManager().beginTransaction(), DeviceSelectFragment.TAG);

    }

    public void closeDeviceSelect() {
        Log.d(MainActivity.LOGGING_TAG, "closeFragment - Close device select dialog fragment");
        this.getDeviceSelectFragment().dismissAllowingStateLoss();
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

        BeaconLogger.trace(device, "Device was selected.");
        this.states = new Stack<>();
        this.selectedDevice = device;
        this.updateStatus(new TransferState(TransferStatus.PREPARE_READOUT));

        this.webView.loadUrl(this.getProperty("beacontransfer.load.address", device.getBeacon().getId()));

    }

    private void startTransfer() {

        Log.i(LOGGING_TAG, "startTransfer()");
        BeaconLogger.debug(this.selectedDevice, "Receiving connection parameters.");

        Beacon beacon = this.selectedDevice.getBeacon();
        this.httpManager.getBeaconSettings(beacon.getId(), new Callback<BeaconSettings>() {
            @Override
            public void call(BeaconSettings beaconSettings) {
                Log.d(LOGGING_TAG, "httpManager.getBeaconSettings(" + beacon + ") successful");
                selectedDevice.getBeacon().setSettings(beaconSettings);
                MainActivity.this.uartManager.start(selectedDevice);
            }

            @Override
            public void error(Throwable t) {
                Dialogs.statusDialog(
                        MainActivity.this,
                        new Status(
                                R.drawable.error_internet_downstream,
                                getString(R.string.error_beacon_settings_fetch_title),
                                getString(R.string.error_beacon_settings_fetch),
                                new StatusAction() {
                                    @Override
                                    public int getStringResource() {
                                        return R.string.retry;
                                    }

                                    @Override
                                    public void onAction(StatusActivity statusActivity) {
                                        MainActivity.this.onDeviceSelected(selectedDevice);
                                        statusActivity.finish();
                                    }
                                }
                        )
                );
            }
        });

    }

    @Override
    public void onUserCancel() {
        this.uartManager.stop(true);
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Cancelling beacon connection...");
        BeaconLogger.send(this);
        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_CANCELLING));
    }

    @Override
    public void onScanStart() {}

    @Override
    public void onScanFailed(int errorCode) {

        Log.e(LOGGING_TAG, "Failed to start BLE scan.");
        Dialogs.statusDialog(
                this,
                new Status(
                        R.drawable.error_general,
                        getString(R.string.error_beacon_scan_title),
                        getResources().getString(R.string.error_beacon_scan) + errorCode,
                        new StatusAction() {
                            @Override
                            public int getStringResource() {
                                return R.string.action_cancel;
                            }

                            @Override
                            public void onAction(StatusActivity statusActivity) {
                                statusActivity.finish();
                                MainActivity.this.closeDeviceSelect();
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
                                MainActivity.this.bluetoothCoordinator.startScan();
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

        if (this.getDeviceSelectFragment() != null) {
            this.getDeviceSelectFragment().updateDevice(device);
        }

    }

    @Override
    public void onNewBluetoothDeviceDiscovered(BluetoothDevice device) {

        if (this.attachBeaconToDevice(device)) {
            Log.d(LOGGING_TAG, "Discovered known beacon: " + device);
            BeaconLogger.trace(device, "Discovered known beacon: " + device);
        }

        if (this.getDeviceSelectFragment() != null) {
            this.getDeviceSelectFragment().addDevice(device);
        }

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
        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_CONNECTING, 20));
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Connecting to beacon.");
    }

    @Override
    public void onDeviceConnected() {
        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_CONNECTED, 25));
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Successfully connected to beacon.");
    }

    @Override
    public void onDeviceDisconnected(boolean isSuccessful) {}

    @Override
    public void onDeviceCommandExecutionStart(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command) {
        if (cancelled) {
            return;
        }
        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_GET_DATA));
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Receiving data from beacon. Executing command: " + command);
    }

    @Override
    public void onDeviceCommandExecutionEnd(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command) {
        if (cancelled) {
            return;
        }
        final int commandExecProgressAmount = 70;
        this.updateStatus(new TransferState(
                TransferStatus.COMM_DEVICE_GET_DATA,
                (int) (this.getCurrentState().getProgress() + ((float) commandExecProgressAmount / (float) totalCommandAmount))
        ));
        BeaconLogger.trace(this.uartManager.getCurrentDevice(), "Received data from beacon. Executed command: " + command);
    }

    @Override
    public void onDeviceExecuted(final BluetoothDevice device) {


        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_SEND_DATA, 90));
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
                updateStatus(new TransferState(TransferStatus.COMM_DEVICE_SEND_DATA, 95));

                Handler h = new Handler(MainActivity.this.getMainLooper());
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        redirectAfterBeacon(device);
                    }
                };
                h.post(r);

            }

            @Override
            public void error(Throwable t) {
                Log.e(LOGGING_TAG, "Failed to send beacon info: " + t.getMessage(), t);
                BeaconLogger.error(device, "Data could not be sent to backend: " + t.getMessage());
                updateStatus(new TransferState(TransferStatus.COMM_DEVICE_SEND_DATA_FAILED));
                Dialogs.statusDialog(
                        MainActivity.this,
                        new Status(
                                R.drawable.error_internet_upstream,
                                getString(R.string.error_beacon_data_send_title),
                                getString(R.string.error_beacon_data_send),
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
    }

    @Override
    public void onDeviceCancelled(BluetoothDevice device) {
        // we are only interested in graceful stops
        if (device != null) {
            this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_CANCELLED));
        }

        this.updateSearchControls();
    }

    @Override
    public void onDeviceExecutionFailed(final BluetoothDevice device) {

        BeaconLogger.send(this);
        this.uartManager.stop(false);

        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_GET_DATA_FAILED, 10));
        this.updateSearchControls();
    }

    private void redirectAfterBeacon(final BluetoothDevice device) {

        BeaconLogger.send(this);

        this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_READOUT_FINISHED, 100));
        this.updateTransferStats();
        this.updateSearchControls();

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
                            getString(R.string.error_connection_failed_title),
                            getString(R.string.error_connection_failed),
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
    public void onWebviewPageFinished(final String url) {
        Log.d(LOGGING_TAG, "onWebviewPageFinished(" + url + ")");
        if (!this.isApiKeyStored) {
            this.webView.evaluateJavascript(
                    "localStorage.setItem('"
                            + this.getProperty("api.key.localStorage.key")
                            + "','"
                            + this.getProperty("api.key")
                            + "'); refreshLogin();",
                    null
            );
            this.isApiKeyStored = true;
        }

        if (this.getCurrentState().getStatus() == TransferStatus.PREPARE_READOUT) {
            Log.d(LOGGING_TAG, "prepare readout");
            if (url.equals(this.getProperty("beacontransfer.load.address", this.selectedDevice.getBeacon().getId()))) {
                Log.d(LOGGING_TAG, "url matches beacontransfer page");
                this.updateStatus(new TransferState(TransferStatus.COMM_DEVICE_GET_SETTINGS, 10));
                this.startTransfer();
            } else {
                Log.d(LOGGING_TAG, "url doesn't match beacontransfer page");
            }
        }
    }

    @Override
    public void updateSearchControls() {

        Handler h = new Handler(getMainLooper());
        Runnable r = new Runnable() {
            @Override
            public void run() {

                if (getBooleanProperty("fab.show") &&
                        prefManager.isTreeDataCollect() &&
                        getCurrentState().getStatus().isShowSearchControls()) {
                    fab.setVisibility(View.VISIBLE);
                }

            }
        };
        h.post(r);

    }

    @Override
    public void onWebviewResouceLoaded() {
    }

    private DeviceSelectFragment getDeviceSelectFragment() {
        return (DeviceSelectFragment) getSupportFragmentManager().findFragmentByTag(DeviceSelectFragment.TAG);
    }

    /**
     * Update status of the frontend and push it to {@link #states}.
     * @param state the state the aplication is currently in
     */
    private void updateStatus(@NonNull TransferState state) {

        TransferState currentState = this.getCurrentState();
        if (state.getStatus() == currentState.getStatus()) {
            currentState.updateWith(state);
            return;
        }
        this.states.push(state);
        Handler h = new Handler(getMainLooper());
        Runnable r = new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(
                        "updateTransferStatus('" + state.getStatus().name() + "');",
                        null
                );
            }
        };
        h.post(r);

    }

    private TransferState getCurrentState() {
        if (this.states == null || this.states.size() == 0) {
            return new TransferState(TransferStatus.NOT_YET_STARTED);
        }
        return this.states.peek();
    }

    /**
     * TODO
     */
    private void updateTransferStats() {

        final int storedLogsAmount = MainActivity.this.uartManager.getSuccessfulCommands().<UARTLogEntry[]>findResponse(UARTResponseType.LOG_ENTRY).getValue().length;

        Handler h = new Handler(getMainLooper());
        Runnable r = new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(
                        "updateTransferStats(" + storedLogsAmount + ");",
                        null
                );
            }
        };
        h.post(r);

    }

}
