package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import java.util.Objects;

import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public class BluetoothDevice {

    private int rssi;

    private Beacon beacon;

    private final android.bluetooth.BluetoothDevice nativeDevice;

    public BluetoothDevice(final android.bluetooth.BluetoothDevice nativeDevice, final int rssi) {
        this(null, nativeDevice);
        this.rssi = rssi;
    }

    public BluetoothDevice(final Beacon beacon, final android.bluetooth.BluetoothDevice nativeDevice) {
        this.beacon = beacon;
        this.nativeDevice = nativeDevice;
    }

    public String getAddress() {
        return this.nativeDevice.getAddress();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public android.bluetooth.BluetoothDevice getNativeDevice() {
        return nativeDevice;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BluetoothDevice)) {
            return false;
        }
        return this.getAddress().equals(((BluetoothDevice) other).getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getAddress());
    }

    @Override
    public String toString() {
        return this.getAddress() + " / " + this.getNativeDevice().getName();
    }
}
