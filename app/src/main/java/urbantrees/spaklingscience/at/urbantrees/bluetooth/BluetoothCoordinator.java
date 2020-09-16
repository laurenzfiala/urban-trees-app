package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.location.SettingInjectorService;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.core.location.LocationManagerCompat;
import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode;
import urbantrees.spaklingscience.at.urbantrees.util.ByteUtils;
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
    private static final String LOG_TAG = BluetoothCoordinator.class.getName();

    private Activity context;

    private OnBluetoothCoordinatorChange listener;

    public BluetoothAdapter bluetoothAdapter;

    private List<String> filterAdresses;

    private AsyncTask scanTask;

    private Set<BluetoothDevice> devices = new LinkedHashSet<BluetoothDevice>();

    public BluetoothCoordinator(Activity context, OnBluetoothCoordinatorChange listener) {
        this.context = context;
        this.listener = listener;
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
     * TODO
     * this triggers request to source activity
     * @return
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

    public void scanForDevices(final List<String> filterAdresses) {

        this.filterAdresses = filterAdresses;

        this.scanTask = new AsyncTask() {
            @Override
            protected Void doInBackground(Object[] objects) {
                startScan();
                return null;
            }
        };
        this.scanTask.execute();

    }

    private Object scanCallback;

    public void startScan() {

        this.listener.onScanStart();

        this.scanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(android.bluetooth.BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothCoordinator.this.onDeviceFound(device, rssi, scanRecord);
            }
        };
        this.bluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) this.scanCallback);

    }

    public void stopScan() {

        if (this.bluetoothAdapter.isEnabled()) {
            this.bluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) this.scanCallback);
        }

        this.devices.clear();

    }

    private void onDeviceFound(android.bluetooth.BluetoothDevice device, int rssi, byte[] scanRecord) {

        final BluetoothDevice bluetoothDevice = new BluetoothDevice(device, rssi, scanRecord);

        if (!this.filterAdresses.contains(bluetoothDevice.getAddress())) {
            return;
        }

        if (this.devices.add(bluetoothDevice)) {
            this.listener.onNewBluetoothDeviceDiscovered(bluetoothDevice);
        }
        this.listener.onBluetoothDeviceDiscovered(bluetoothDevice);

    }

    public interface OnBluetoothCoordinatorChange {
        void onScanStart();
        void onBluetoothDeviceDiscovered(BluetoothDevice device);
        void onNewBluetoothDeviceDiscovered(BluetoothDevice device);
    }

}
