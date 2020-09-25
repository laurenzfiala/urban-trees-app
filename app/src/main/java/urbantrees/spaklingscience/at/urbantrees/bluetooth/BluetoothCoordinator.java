package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.core.location.LocationManagerCompat;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;

/**
 * High level abstraction layer used to harden bluetooth
 * connection handling, discovery and permission requests.
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class BluetoothCoordinator {

    /**
     * Logging tag for this class.
     */
    private static final String     LOG_TAG = BluetoothCoordinator.class.getName();

    private Activity context;
    private OnBluetoothCoordinatorChange listener;

    public BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;

    private List<ScanFilter> scanFilters;
    private ScanCallback scanCallback;

    private Set<BluetoothDevice> devices = new LinkedHashSet<>();

    public BluetoothCoordinator(Activity context,
                                OnBluetoothCoordinatorChange listener,
                                List<ScanFilter> scanFilters) {
        this.context = context;
        this.listener = listener;
        this.scanFilters = scanFilters;
        this.getBluetoothAdapter();
    }

    private void getBluetoothAdapter() {
        final BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * If the {@link #bluetoothAdapter} is disabled, sends a request intent for
     * enabling bluetooth to the OS.
     * @return If the user agreed, return true; otherwise false.
     * @throws RuntimeException if {@link #bluetoothAdapter} is null.
     */
    public boolean enableBluetooth() throws RuntimeException {

        if (this.bluetoothAdapter == null) {

            Log.e(LOG_TAG, "Device does not support bluetooth, adapter is null.");
            throw new RuntimeException("Could not enable bluetooth.");

        } else if (!this.bluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "Bluetooth is turned off. Requesting activation.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.context.startActivityForResult(enableBtIntent, ActivityResultCode.INTENT_REQUEST_ENABLE_BLUETOOTH);

            return false;

        }

        return true;

    }

    /**
     * Checks if the devices' location service may be accessed. If not, an info dialog is shown t
     * the user. If in turn this dialog is acceppted, the user is redirected to the location
     * settings where they can activate location services.
     * Upon returning from the settings page, if the location was turned on, an intent result will
     * be triggered in {@link #context}.
     * @return true if location is already on; false otherwise.
     */
    public boolean enableLocation() throws RuntimeException {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return true;
        }

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (LocationManagerCompat.isLocationEnabled(lm)) {
            return true;
        } else {
            Log.d(LOG_TAG, "Location is turned off. Informing user.");
            Dialogs.dialog(this.context,
                    R.layout.view_dialog_enable_location,
                    R.string.enable_location,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent showLocationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            BluetoothCoordinator.this.context.startActivityForResult(
                                    showLocationSettingsIntent,
                                    ActivityResultCode.INTENT_LOCATION_SOURCE_SETTINGS
                            );
                        }
                    },
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
        }
        return false;

    }

    public void startScan() {

        this.listener.onScanStart();

        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(LOG_TAG, "onScanResult - discovered device: " + result.getDevice().getAddress());
                BluetoothCoordinator.this.onDeviceFound(result);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(LOG_TAG, "onScanFailed - failed to start scan: error code " + errorCode);
                BluetoothCoordinator.this.listener.onScanFailed(errorCode);
            }
        };

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsBuilder
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }

        this.scanner = this.bluetoothAdapter.getBluetoothLeScanner();
        Log.d(LOG_TAG, "startScan - start scanning");
        this.scanner.startScan(this.scanFilters, settingsBuilder.build(), this.scanCallback);

    }

    public void stopScan() {

        if (this.bluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "stopScan - stop scanning");
            this.scanner.stopScan(this.scanCallback);
        }

        this.devices.clear();

    }

    private void onDeviceFound(ScanResult result) {

        final BluetoothDevice bluetoothDevice = new BluetoothDevice(
                result.getDevice(),
                result.getRssi(),
                Objects.requireNonNull(result.getScanRecord()).getBytes()
        );

        if (this.devices.add(bluetoothDevice)) {
            Log.d(LOG_TAG, "onDeviceFound - Discovered new device: " + bluetoothDevice.getAddress());
            this.listener.onNewBluetoothDeviceDiscovered(bluetoothDevice);
        } else {
            Log.d(LOG_TAG, "onDeviceFound - Re-discovered device: " + bluetoothDevice.getAddress());
        }
        this.devices.remove(bluetoothDevice);
        this.devices.add(bluetoothDevice);
        this.listener.onBluetoothDeviceDiscovered(bluetoothDevice);

    }

    public interface OnBluetoothCoordinatorChange {
        void onScanStart();
        void onScanFailed(int errorCode);
        void onBluetoothDeviceDiscovered(BluetoothDevice device);
        void onNewBluetoothDeviceDiscovered(BluetoothDevice device);
    }

}
