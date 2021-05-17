package urbantrees.spaklingscience.at.urbantrees.util;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconLog;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconLogSeverity;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconLogType;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconStatus;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandler;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerMethod;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerParams;
import urbantrees.spaklingscience.at.urbantrees.http.HttpHandlerResult;
import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;

/**
 * Keeps all accumulated beacon logs and send them to the backend.
 * @author Laurenz Fiala
 * @since 2019/05/19
 */
public class BeaconLogger {

    private static final String LOGGING_TAG = BeaconLogger.class.getName();

    private static List<BeaconLog> logs = Collections.synchronizedList(new ArrayList<BeaconLog>());

    private static void log(int beaconId, BeaconLogSeverity severity, String message) {
        logs.add(new BeaconLog(beaconId, severity, BeaconLogType.ANDR_APP, message, new Date()));
    }

    public static void trace(BluetoothDevice device, String message) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device or beacon undefined.");
            return;
        }
        trace(device.getBeacon().getId(), message);
    }

    public static void trace(Beacon beacon, String message) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        trace(beacon.getId(), message);
    }

    public static void trace(int beaconId, String message) {
        log(beaconId, BeaconLogSeverity.TRACE, message);
    }

    public static void debug(BluetoothDevice device, String message) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        debug(device.getBeacon().getId(), message);
    }

    public static void debug(Beacon beacon, String message) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        debug(beacon.getId(), message);
    }

    public static void debug(int beaconId, String message) {
        log(beaconId, BeaconLogSeverity.DEBUG, message);
    }

    public static void info(BluetoothDevice device, String message) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        info(device.getBeacon().getId(), message);
    }

    public static void info(Beacon beacon, String message) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        info(beacon.getId(), message);
    }

    public static void info(int beaconId, String message) {
        log(beaconId, BeaconLogSeverity.INFO, message);
    }

    public static void warn(BluetoothDevice device, String message) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        warn(device.getBeacon().getId(), message);
    }

    public static void warn(Beacon beacon, String message) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        warn(beacon.getId(), message);
    }

    public static void warn(int beaconId, String message) {
        log(beaconId, BeaconLogSeverity.WARN, message);
    }

    public static void error(BluetoothDevice device, String message) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        error(device.getBeacon().getId(), message);
    }

    public static void error(Beacon beacon, String message) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't log beacon message, device identifier undefined.");
            return;
        }
        error(beacon.getId(), message);
    }

    public static void error(int beaconId, String message) {
        log(beaconId, BeaconLogSeverity.ERROR, message);
    }

    /**
     * Sends all beacon logs to the backend. If successful,
     * #logs is cleared. Spawns a new thread to do so.
     * @param props {@link ApplicationProperties} from the callers' context
     */
    public static void send(final ApplicationProperties props) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                BeaconLogger.sendThreaded(props);
            }
        };
        Thread t = new Thread(r);
        t.start();

    }

    /**
     * Sends all beacon logs to the backend. If successful,
     * #logs is cleared.
     * @param props {@link ApplicationProperties} from the callers' context
     */
    private synchronized static void sendThreaded(final ApplicationProperties props) {

        if (logs.size() == 0) {
            Log.d(LOGGING_TAG, "Not sending beacon logs to backend. No logs to send.");
            return;
        }

        Log.d(LOGGING_TAG, "Sending " + logs.size() + " beacon logs to backend...");

        try {

            String payload = new ObjectMapper().writeValueAsString(logs.toArray());

            HttpHandler f = new HttpHandler();
            f.execute(
                    new HttpHandlerParams(
                            props.getProperty("beacon.logs.url"),
                            HttpHandlerMethod.PUT,
                            HttpManager.getHttpHeaders(),
                            payload
                    )
            );
            HttpHandlerResult.isSuccessfulElseThrow(f.get());
            logs.clear();

        } catch(Throwable t) {
            Log.e(LOGGING_TAG, "Could not send beacon logs: " + t.getMessage());
            return;
        }

        Log.d(LOGGING_TAG, "Successfully sent " + logs.size() + " beacon logs to backend.");

    }

    /**
     * @see #status(int, BeaconStatus, ApplicationProperties)
     */
    public static void status(BluetoothDevice device, BeaconStatus status, final ApplicationProperties props) {
        if (device == null || device.getBeacon() == null) {
            Log.w(LOGGING_TAG, "Can't update beacon status, device identifier undefined.");
            return;
        }
        status(device.getBeacon().getId(), status, props);
    }

    /**
     * @see #status(int, BeaconStatus, ApplicationProperties)
     */
    public static void status(Beacon beacon, BeaconStatus status, final ApplicationProperties props) {
        if (beacon == null) {
            Log.w(LOGGING_TAG, "Can't update beacon status, device identifier undefined.");
            return;
        }
        status(beacon.getId(), status, props);
    }

    /**
     * Sets beacon status to the given status in the backend.
     * @param props {@link ApplicationProperties} from the callers' context
     */
    public static void status(final int beaconId, final BeaconStatus status, final ApplicationProperties props) {

        if (status == null) {
            throw new RuntimeException("Beacon status to update is null.");
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                BeaconLogger.statusThreaded(beaconId, status, props);
            }
        };
        Thread t = new Thread(r);
        t.start();

    }

    /**
     * Sends all beacon logs to the backend. If successful,
     * #logs is cleared.
     * @param beaconId Identifier of beaocn to update
     * @param status Status to update to
     * @param props {@link ApplicationProperties} from the callers' context
     */
    private synchronized static void statusThreaded(int beaconId, BeaconStatus status, final ApplicationProperties props) {

        Log.d(LOGGING_TAG, "Updating beacon " + beaconId + " to status " + status + " in backend...");

        try {

            HttpHandler f = new HttpHandler();
            f.execute(
                    new HttpHandlerParams(
                            props.getProperty("beacon.status.update")
                                    .replace("{beaconId}", String.valueOf(beaconId))
                                    .replace("{status}", status.toString()),
                            HttpHandlerMethod.PUT,
                            HttpManager.getHttpHeaders(),
                            status.toString()
                    )
            );
            HttpHandlerResult.isSuccessfulElseThrow(f.get());

        } catch(Throwable t) {
            Log.e(LOGGING_TAG, "Could not update beacon status: " + t.getMessage());
            return;
        }

        Log.d(LOGGING_TAG, "Successfully updated beacon " + beaconId + " to status " + status + " in backend.");

    }

}
