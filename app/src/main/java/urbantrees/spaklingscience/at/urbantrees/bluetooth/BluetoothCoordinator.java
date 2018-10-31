package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode;
import urbantrees.spaklingscience.at.urbantrees.util.HasContext;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeEmitter;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeType;

/**
 * High level abstraction layer used to harden bluetooth
 * connection handling, discovery and permission requests.
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class BluetoothCoordinator extends PropertyChangeEmitter {

    /**
     * Logging tag for this class.
     */
    private static final String LOG_TAG = BluetoothCoordinator.class.getName();

    /**
     * The interval in milliseconds to scan for bluetooth beacons.
     * See {@link #BT_SCAN_INTERVAL}.
     *
     * TODO implement logic
     */
    private static final int BT_SCAN_DURATION = 5000;

    /**
     * The interval in milliseconds to start scanning for bluetooth beacons.
     * See {@link #BT_SCAN_DURATION}.
     *
     * TODO implement logic
     */
    private static final int BT_SCAN_INTERVAL = 10000;

    public BluetoothAdapter bluetoothAdapter;

    private List<String> filterAdresses;

    private AsyncTask scanTask;

    private BluetoothDevice nearestDevice;

    public BluetoothCoordinator(Activity context) {
        super(context);

        this.getBluetoothAdapter();

    }

    /**
     * TODO
     */
    private void getBluetoothAdapter() {

        final BluetoothManager bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

    }

    /**
     * TODO
     * this triggers request to source activity
     * @return
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

    public void startScan() {

        if (Build.VERSION.SDK_INT >= 21) { // android lollipop and higher

            this.bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {

                @Override
                @TargetApi(21)
                public void onScanResult(int callbackType, ScanResult result) {

                    BluetoothCoordinator.this.onDeviceFound(result.getDevice(), result.getRssi());

                    super.onScanResult(callbackType, result);
                }
            });

        } else {

            this.bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(android.bluetooth.BluetoothDevice device, int rssi, byte[] scanRecord) {

                    BluetoothCoordinator.this.onDeviceFound(device, rssi);

                }
            });

        }

    }

    private void onDeviceFound(android.bluetooth.BluetoothDevice device, int rssi) {

        final BluetoothDevice bluetoothDevice = new BluetoothDevice(device, rssi);

        if (!this.filterAdresses.contains(bluetoothDevice.getAddress())) {
            return;
        }
        if (this.nearestDevice == null || !bluetoothDevice.equals(this.nearestDevice)) {
            this.nearestDevice = bluetoothDevice;
            this.notify(PropertyChangeType.BLUETOOTH_DEVICE, bluetoothDevice);
        } else if (bluetoothDevice.equals(this.nearestDevice)) {
            this.nearestDevice.setRssi(rssi);
        }

    }

    /**
     * Unset nearest device so the scan can
     * continue searching for all devices including
     * the failed one.
     */
    public void resetNearestDevice() {
        this.nearestDevice = null;
    }

}
