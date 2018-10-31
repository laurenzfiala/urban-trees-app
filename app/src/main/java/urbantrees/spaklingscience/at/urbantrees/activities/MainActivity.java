package urbantrees.spaklingscience.at.urbantrees.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.callback.CallbackHandler;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTManager;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponse;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandler;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerMethod;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerParams;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerResult;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHeader;
import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeType;

import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE_ARRAY;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.INTENT_REQUEST_ENABLE_BLUETOOTH;

public class MainActivity extends AppCompatActivity implements PropertyChangeListener {

    private static final String LOGGING_TAG = MainActivity.class.getName();

    /**
     * The {@link WebView} to show the webpage in.
     */
    private WebView webView;

    private ValueCallback<Uri> uploadMessage;

    private ValueCallback<Uri[]> uploadMessageArray;

    private UARTManager uartManager;

    private HttpManager httpManager;

    private Properties props;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.loadProperties();

        this.httpManager = new HttpManager(this);
        this.httpManager.setApiKeyHeaderKey(this.props.getProperty("api.key.header.key"));
        this.httpManager.setApiKeyHeaderValue(this.props.getProperty("api.key"));
        this.httpManager.setDeviceListUrl(this.props.getProperty("beacon.list.url"));
        this.httpManager.setBeaconSettingsUrl(this.props.getProperty("beacon.settings.url"));

        //WebView.setWebContentsDebuggingEnabled(true);

        this.webView = (WebView) findViewById(R.id.web_view);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.setWebChromeClient(this.getWebChromeClient());
        //this.webView.clearCache(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        String cookieString = this.httpManager.getApiKeyHeaderKey() + "=" + this.httpManager.getApiKeyHeaderValue() + "; Domain=" + this.props.getProperty("api.key.domain");
        cookieManager.setCookie(this.props.getProperty("api.key.domain"), cookieString);

        this.webView.clearFormData();
        this.webView.loadUrl(this.props.getProperty("initial.load.address"));

        MainActivity.this.showProgress(1, R.string.http_fetch_devices);
        this.httpManager.fetchDeviceList(new Callback<Void>() {

            @Override
            public void call(Void v) {
                MainActivity.this.uartManager = new UARTManager(MainActivity.this);
                MainActivity.this.uartManager.listen(PropertyChangeType.UART_MANAGER_STATUS, MainActivity.this);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (MainActivity.this.uartManager.enableBluetooth()) {
                            MainActivity.this.uartManager.fetchDeviceInfo(MainActivity.this.httpManager.getAllowedDeviceAddresses());
                        }
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }

            @Override
            public void error(Throwable t) {
                Log.e(LOGGING_TAG, "Error fetching device list: " + t.getMessage(), t);
                Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_list_fetch_failed));

            }

        });

    }

    /**
     * Load config file.
     */
    private void loadProperties() {

        this.props = new Properties();

        try {
            props.load(this.getClass().getResourceAsStream("/assets/config.properties"));
        } catch (IOException e) {
            Log.e(LOGGING_TAG, "Could not load config: " + e.getMessage(), e);
        }

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

    /**
     * {@link WebChromeClient} with inplemented filechooser event handlers which
     * are needed for working input type=file in the website.
     *
     * @return Newly created {@link WebChromeClient}
     */
    private WebChromeClient getWebChromeClient() {
        return new WebChromeClient() {

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                MainActivity.this.openFileChooserImpl(null, uploadMsg);
                return true;
            }

        };
    }

    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg, ValueCallback<Uri[]> uploadMsgArray) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");

        int resultCode;
        if (uploadMsg != null) {
            this.uploadMessage = uploadMsg;
            resultCode = FILECHOOSER_RESULT_CODE;
        } else {
            this.uploadMessageArray = uploadMsgArray;
            resultCode = FILECHOOSER_RESULT_CODE_ARRAY;
        }
        MainActivity.this.startActivityForResult(Intent.createChooser(i, "Choose a photo"), resultCode);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case FILECHOOSER_RESULT_CODE:

            {
                if (this.uploadMessage == null) {
                    return;
                }

                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                this.uploadMessage.onReceiveValue(result);
                this.uploadMessage = null;

                break;
            }

            case FILECHOOSER_RESULT_CODE_ARRAY:

            {
                if (this.uploadMessageArray == null) {
                    return;
                }

                Uri result = null;
                if (intent != null && resultCode == Activity.RESULT_OK) {
                    result = intent.getData();
                }

                if (result != null) {
                    this.uploadMessageArray.onReceiveValue(new Uri[]{result});
                } else {
                    this.uploadMessageArray.onReceiveValue(new Uri[]{});
                }
                this.uploadMessageArray = null;

                break;
            }

            case INTENT_REQUEST_ENABLE_BLUETOOTH:

            {
                this.uartManager.fetchDeviceInfo(this.httpManager.getAllowedDeviceAddresses());

                break;
            }

        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.i(LOGGING_TAG, "New UART status received: " + PropertyChangeType.UART_MANAGER_STATUS.cast(evt.getNewValue()).name());

        int stringId = R.string.app_name;
        boolean showProgress = false;
        int step = -1;

        switch (PropertyChangeType.UART_MANAGER_STATUS.cast(evt.getNewValue())) {
            case DEVICE_CONNECTING:
                stringId = R.string.ble_device_searching;
                showProgress = true;
                step = 2;
                break;
            case DEVICE_FOUND:
                stringId = R.string.ble_device_connected;
                step = 3;

                Beacon beacon = this.httpManager.getBeaconByAddress(this.uartManager.getCurrentDevice().getAddress());
                this.httpManager.getBeaconSettings(beacon.getId(), new Callback<Beacon>() {
                    @Override
                    public void call(Beacon beacon) {
                        MainActivity.this.uartManager.setCurrentBeacon(beacon);
                        MainActivity.this.uartManager.populateCommands();
                        MainActivity.this.uartManager.connectAndExecuteCommand();
                    }

                    @Override
                    public void error(Throwable t) {
                        Log.e(LOGGING_TAG, t.getMessage(), t);
                        Dialogs.errorPrompt(MainActivity.this, getString(R.string.beacon_settings_fetch_failed));
                    }
                });

                break;
            case DEVICE_CONNECTION_FAILED:
                stringId = R.string.ble_device_connection_failed;
                break;
            case DEVICE_INFO_FETCH:
                stringId = R.string.ble_uart_fetching;
                showProgress = true;
                step = 4;
                break;
            case DEVICE_INFO_FETCH_FAILED:
                stringId = R.string.ble_uart_fetch_failed;
                Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

                Runnable redirectRunnable = new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.webView.loadUrl(MainActivity.this.props.getProperty("beacontransfer.load.address.failed").replaceAll("\\{treeId\\}", MainActivity.this.uartManager.getCurrentBeacon().getTreeId() + ""));
                    }
                };
                mainHandler.post(redirectRunnable);

                break;
            case DEVICE_INFO_FETCHED:
                stringId = R.string.http_sending_data;
                step = 5;
                try {
                    this.sendBeaconData(new Callback<Void>() {
                        @Override
                        public void call(Void v) {
                            Log.i(LOGGING_TAG, "Successfully sent beacon data to server, redirecting to beacon page");
                            Dialogs.dismissSnackbar();

                            Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

                            Runnable redirectRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.webView.loadUrl(MainActivity.this.props.getProperty("beacontransfer.load.address").replaceAll("\\{treeId\\}", MainActivity.this.uartManager.getCurrentBeacon().getTreeId() + ""));
                                }
                            };
                            mainHandler.post(redirectRunnable);
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
                break;
        }

        if (showProgress) {
            this.showProgress(step, stringId);
        }

    }

    /**
     * TODO
     * @param step
     * @param textStringId
     */
    private void showProgress(int step, int textStringId) {

        String stepText = "";
        if (step != -1) {
            stepText = step + "/5  ";
        }
        Dialogs.progressSnackbar(this.findViewById(R.id.layout_root), stepText + this.getString(textStringId));

    }

    // TODO move to httpmanager
    private void sendBeaconData(Callback callback) throws JsonProcessingException {

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(this.httpManager.getApiKeyHeaderKey(), this.httpManager.getApiKeyHeaderValue());

        List<Object> datasets = new ArrayList<Object>();
        for (UARTResponse response : UARTManager.LOGGER_COMMAND.getResponses()) {
            for (UARTLogEntry entry : (UARTLogEntry[]) response.getValue()) {
                datasets.add(entry);
            }
        }

        if (datasets.size() == 0) {
            Log.i(LOGGING_TAG, "No datasets received from beacon. Not uploading any.");
            callback.call(null);
            return;
        }

        String test = new ObjectMapper().writeValueAsString(datasets);

        HttpHandler f = new HttpHandler(this);
        try {
            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacontransfer.api").replaceFirst("\\{beaconId\\}", this.uartManager.getCurrentBeacon().getId() + ""),
                            HttpHandlerMethod.PUT,
                            headers,
                            test
                    )
            );

            callback.call(null);
        }catch (Throwable t) {
            callback.error(t);
        }

    }

}
