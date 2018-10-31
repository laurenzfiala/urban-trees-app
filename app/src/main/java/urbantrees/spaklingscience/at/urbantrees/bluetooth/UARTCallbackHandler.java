package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeEmitter;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeType;

public class UARTCallbackHandler extends BluetoothGattCallback {

    /**
     * TODO we need to do this since bluetoothgattcallback is abstract as well
     */
    private PropertyChangeEmitter propertyEmitter;

    private UARTManager manager;

    private UARTDevice device;

    public UARTCallbackHandler(Activity context, UARTManager manager) {
        this.propertyEmitter = new PropertyChangeEmitter(context);
        this.manager = manager;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        this.propertyEmitter.notify(PropertyChangeType.GATT_STATUS, newState);

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        if (!this.device.isTxNotificationEnabled()) {

            this.device.resolveCharacteristics(gatt);

            boolean isNotifyOK = gatt.setCharacteristicNotification(device.getTxCharacteristic(), true);

            device.getTxNotificationCharacteristic().setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean isDescWriteOK = gatt.writeDescriptor(device.getTxNotificationCharacteristic());

            this.device.setTxNotificationEnabled(isNotifyOK && isDescWriteOK);

        }

        super.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        try {

            UARTCommand command = this.manager.getCurrentCommand();
            command.nextTry();

            final byte[] sendVal = command.getCommandBytes();
            BluetoothGattCharacteristic rxChar = this.device.getRxCharacteristic();

            rxChar.setValue(sendVal);
            gatt.writeCharacteristic(rxChar);

        } catch (Throwable t) {
            this.getPropertyEmitter().notify(PropertyChangeType.THROWABLE, t);
        }

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        this.getPropertyEmitter().notify(PropertyChangeType.GATT_CHARACTERISTIC, characteristic);

    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        this.getPropertyEmitter().notify(PropertyChangeType.GATT_CHARACTERISTIC, characteristic);

    }

    public UARTDevice getDevice() {
        return device;
    }

    public void setDevice(UARTDevice device) {
        this.device = device;
    }

    public PropertyChangeEmitter getPropertyEmitter() {
        return propertyEmitter;
    }

}
