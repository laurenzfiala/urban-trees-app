package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public class UARTCallbackHandler extends BluetoothGattCallback {

    private OnUARTCallbackHandlerChange listener;

    private UARTManager manager;

    private UARTDevice device;

    private boolean disabled = false;

    public UARTCallbackHandler(UARTManager manager, OnUARTCallbackHandlerChange listener) {
        this.manager = manager;
        this.listener = listener;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (this.disabled) {
            return;
        }

        this.listener.onGattStatusChanged(status, newState);

        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        }

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        if (this.disabled) {
            return;
        }

        //if (!this.device.isTxNotificationEnabled()) {

            this.device.resolveCharacteristics(gatt);

            boolean isNotifyOK = gatt.setCharacteristicNotification(device.getTxCharacteristic(), true);

            device.getTxNotificationCharacteristic().setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean isDescWriteOK = gatt.writeDescriptor(device.getTxNotificationCharacteristic());

            //this.device.setTxNotificationEnabled(isNotifyOK && isDescWriteOK);

        //}

        super.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        if (this.disabled) {
            return;
        }

        try {

            UARTCommand command = this.manager.getCurrentCommand();
            //command.nextTry();

            final byte[] sendVal = command.getCommandBytes();
            BluetoothGattCharacteristic rxChar = this.device.getRxCharacteristic();

            rxChar.setValue(sendVal);
            gatt.writeCharacteristic(rxChar);

        } catch (Throwable t) {
            this.listener.onGattCharacteristicWriteFailed(t, descriptor, status);
        }

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        if (this.disabled) {
            return;
        }
        this.listener.onGattCharacteristicChange(characteristic);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        if (this.disabled) {
            return;
        }
        this.listener.onGattCharacteristicChange(characteristic);
    }

    public UARTDevice getDevice() {
        return device;
    }

    public void setDevice(UARTDevice device) {
        this.device = device;
    }

    public interface OnUARTCallbackHandlerChange {
        void onGattCharacteristicChange(BluetoothGattCharacteristic characteristic);
        void onGattCharacteristicWriteFailed(Throwable t, BluetoothGattDescriptor descriptor, int status);
        void onGattStatusChanged(int status, int newState);
    }

    public void disable() {
        this.disabled = true;
    }

}
