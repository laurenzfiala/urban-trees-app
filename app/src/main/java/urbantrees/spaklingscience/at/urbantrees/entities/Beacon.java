package urbantrees.spaklingscience.at.urbantrees.entities;

/**
 * DTO for beacon object.
 * @author Laurenz Fiala
 * @since 2018/10/26
 */
public class Beacon {

    private int id;
    private String deviceId;
    private TreeLight tree;
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

    public TreeLight getTree() {
        return tree;
    }

    public void setTree(TreeLight tree) {
        this.tree = tree;
    }

    public int getTreeId() {
        return this.tree.getId();
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

    @Override
    public String toString() {
        return "Beacon{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", tree=" + tree +
                ", bluetoothAddress='" + bluetoothAddress + '\'' +
                ", status='" + status + '\'' +
                ", settings=" + settings +
                '}';
    }
}
