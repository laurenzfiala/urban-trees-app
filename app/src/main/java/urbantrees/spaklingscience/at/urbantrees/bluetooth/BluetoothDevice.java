package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import java.util.Objects;

import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;

/**
 * Main class used for devices throughout the app.
 * Holds important connection info and (optionally) a {@link Beacon}.
 *
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public class BluetoothDevice {

    private int rssi;

    private byte[] advertisementPkg;

    private Beacon beacon;

    private final android.bluetooth.BluetoothDevice nativeDevice;

    private long dataReadoutTime;

    public BluetoothDevice(final android.bluetooth.BluetoothDevice nativeDevice,
                           final int rssi,
                           final byte[] advertisementPkg) {
        this.nativeDevice = nativeDevice;
        this.rssi = rssi;
        this.advertisementPkg = advertisementPkg;
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

    public byte[] getAdvertisementPkg() {
        return advertisementPkg;
    }

    public void setAdvertisementPkg(byte[] advertisementPkg) {
        this.advertisementPkg = advertisementPkg;
    }

    public long getDataReadoutTime() {
        return dataReadoutTime;
    }

    public void setDataReadoutTime(long dataReadoutTime) {
        this.dataReadoutTime = dataReadoutTime;
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
