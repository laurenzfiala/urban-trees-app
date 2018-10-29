package urbantrees.spaklingscience.at.urbantrees.entities;

/**
 * DTO for beacon object.
 * @author Laurenz Fiala
 * @since 2018/10/26
 */
public class Beacon {

    private int id;
    private String deviceId;
    private int treeId;
    private String bluetoothAddress;
    private String status;
    private BeaconSettings settings;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BeaconSettings getSettings() {
        return settings;
    }

    public void setSettings(BeaconSettings settings) {
        this.settings = settings;
    }
}
