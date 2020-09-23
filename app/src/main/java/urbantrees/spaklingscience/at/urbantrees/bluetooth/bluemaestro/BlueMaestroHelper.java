package urbantrees.spaklingscience.at.urbantrees.bluetooth.bluemaestro;

import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommand;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTDevice;
import urbantrees.spaklingscience.at.urbantrees.util.ByteUtils;

/**
 * Helper fields/methods which are specific to Blue Maestro Bluetooth beacons.
 *
 * @author Laurenz Fiala
 * @since 2018/06/01
 */
public class BlueMaestroHelper {

    public static final int BLUETOOTH_SIG_IDENTIFIER = 0x0133;

    /**
     * Builds filters that match Blue Maestro's UART Bluetooth beacons.
     * Note:    we do not filter for the UART service UUID because this requires the beacon to
     *          advertise that information (which it doesn't). See commented line.
     * @return List of {@link ScanFilter}s to hand to the {@link android.bluetooth.le.BluetoothLeScanner}.
     */
    public static List<ScanFilter> getBeaconScanFilters() {

        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder()
                .setManufacturerData(BlueMaestroHelper.BLUETOOTH_SIG_IDENTIFIER, new byte[]{});
                //.setServiceUuid(new ParcelUuid(UARTDevice.SERVICE_UUID));
        scanFilters.add(scanFilterBuilder.build());

        return scanFilters;

    }

}
