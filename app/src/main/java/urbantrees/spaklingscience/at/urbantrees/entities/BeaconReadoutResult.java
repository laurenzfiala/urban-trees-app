package urbantrees.spaklingscience.at.urbantrees.entities;

import java.util.ArrayList;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;

/**
 * DTO for beacon datasets and settings to be sent to backend.
 * @author Laurenz Fiala
 * @since 2019/05/20
 */
public class BeaconReadoutResult {

    private UARTLogEntry[] datasets;
    private BeaconSettings settings;
    private long timeSinceDataReadoutMs;

    public BeaconReadoutResult(UARTLogEntry[] datasets, BeaconSettings settings, long timeSinceDataReadoutMs) {
        this.datasets = datasets;
        this.settings = settings;
        this.timeSinceDataReadoutMs = timeSinceDataReadoutMs;
    }

    public UARTLogEntry[] getDatasets() {
        return datasets;
    }

    public BeaconSettings getSettings() {
        return settings;
    }

    public long getTimeSinceDataReadoutMs() {
        return timeSinceDataReadoutMs;
    }
}
