package urbantrees.spaklingscience.at.urbantrees.util;

import android.bluetooth.BluetoothGattCharacteristic;

import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTManagerStatus;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public class PropertyChangeType {

    public static final Class<UARTManagerStatus> UART_MANAGER_STATUS = UARTManagerStatus.class;
    public static final Class<Integer> GATT_STATUS = Integer.class;
    public static final Class<BluetoothDevice> BLUETOOTH_DEVICE = BluetoothDevice.class;
    public static final Class<BluetoothGattCharacteristic> GATT_CHARACTERISTIC = BluetoothGattCharacteristic.class;

    public static final Class<Throwable> THROWABLE = Throwable.class;

    public static boolean equals(final String propertyName, final Class propertyType) {
        return propertyName.equals(propertyType.getName());
    }

    public static String getName(final Class propertyType) {
        return propertyType.getName();
    }

}
