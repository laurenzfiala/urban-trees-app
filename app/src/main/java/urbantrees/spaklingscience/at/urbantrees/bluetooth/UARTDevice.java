package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.UUID;

public class UARTDevice extends urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice {

    /**
     * Nordic UART service UUID.
     */
    public static final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");

    /**
     * Remote devices' transfer characteristic UUID.
     */
    public static final UUID TX_CHAR_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    /**
     * Remote devices' transfer notification descriptor UUID.
     * Used to receive results from the remote device.
     */
    public static final UUID TX_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * Remote devices' receive characteristic UUID.
     */
    public static final UUID RX_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");

    private BluetoothGattCharacteristic rxCharacteristic;

    private BluetoothGattCharacteristic txCharacteristic;

    private BluetoothGattDescriptor txNotificationCharacteristic;

    private boolean txNotificationEnabled;

    public UARTDevice(final urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice bluetoothDevice) {
        super(bluetoothDevice.getDevice(), bluetoothDevice.getRssi());
    }

    public UARTDevice(final urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice bluetoothDevice, final BluetoothGatt gatt) {
        super(bluetoothDevice.getDevice(), bluetoothDevice.getRssi());
        this.resolveCharacteristics(gatt);
    }

    public BluetoothGattCharacteristic getRxCharacteristic() {
        return rxCharacteristic;
    }

    public BluetoothGattCharacteristic getTxCharacteristic() {
        return txCharacteristic;
    }

    public BluetoothGattDescriptor getTxNotificationCharacteristic() {
        return txNotificationCharacteristic;
    }

    public void resolveCharacteristics(final BluetoothGatt gatt) throws RuntimeException {

        final BluetoothGattService gattService = gatt.getService(SERVICE_UUID);
        if (gattService == null) {
            throw new RuntimeException("Could not get GATT UART service from device '" + this + "'.");
        }

        this.rxCharacteristic = gattService.getCharacteristic(RX_CHAR_UUID);
        this.txCharacteristic = gattService.getCharacteristic(TX_CHAR_UUID);
        this.txNotificationCharacteristic = this.txCharacteristic.getDescriptor(TX_NOTIFICATION_DESCRIPTOR_UUID);

    }

    public boolean isTxNotificationEnabled() {
        return txNotificationEnabled;
    }

    public void setTxNotificationEnabled(boolean txNotificationEnabled) {
        this.txNotificationEnabled = txNotificationEnabled;
    }
}
