package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public class BluetoothDevice {

    private int rssi;

    private final android.bluetooth.BluetoothDevice device;

    public BluetoothDevice(final android.bluetooth.BluetoothDevice device, final int rssi) {
        this(device);
        this.rssi = rssi;
    }

    public BluetoothDevice(final android.bluetooth.BluetoothDevice device) {
        this.device = device;
    }

    public String getAddress() {
        return this.device.getAddress();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public android.bluetooth.BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BluetoothDevice)) {
            return false;
        }
        return this.getAddress().equals(((BluetoothDevice) other).getAddress());
    }

    @Override
    public String toString() {
        return this.getAddress() + " / " + this.getDevice().getName();
    }
}
