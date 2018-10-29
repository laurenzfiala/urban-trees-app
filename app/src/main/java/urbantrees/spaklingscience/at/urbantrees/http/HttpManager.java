package urbantrees.spaklingscience.at.urbantrees.http;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import urbantrees.spaklingscience.at.urbantrees.activities.MainActivity;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.HasContext;

/**
 * TODO doc
 * @author Laurenz Fiala
 * @since 2018/10/27
 */
public class HttpManager extends HasContext {

    private static final String LOGGING_TAG = HttpManager.class.getName();

    private Beacon[] beacons;

    public HttpManager(Activity context) {
        super(context);
    }

    /**
     * HTTP-GET all working beacons to filter for.
     * @param callbackFn Executed upon successful execution.
     */
    public void fetchDeviceList(Callback<Void> callbackFn) {

        // TODO make threaded (currently blocks startup)
        HttpHandler f = new HttpHandler(this.context);

        try {

            f.execute(
                    new HttpHandlerParams(
                            "http://192.168.1.100/beacon/all/status/OK",
                            HttpHandlerMethod.GET,
                            new HttpHeader[]{new HttpHeader(MainActivity.HEADER_KEY_AUTH, MainActivity.HEADER_VALUE_AUTH)},
                            null
                    )
            );

            HttpHandlerResult res = f.get();

            Log.d(LOGGING_TAG, "Fetched beacon list from remote successfully.");

            this.beacons = new ObjectMapper().readValue(res.getResponseValue(), Beacon[].class);

            List<String> allowedBluetoothAdresses = new ArrayList<String>();
            for (Beacon b : beacons) {
                allowedBluetoothAdresses.add(b.getBluetoothAddress());
            }

            callbackFn.call(null);

        } catch (Throwable t) {
            Log.e(LOGGING_TAG, t.getMessage(), t);
            callbackFn.error(t);
        }

    }

    /**
     * HTTP-GET beacon settings for the currently connected beacon.
     * @param beaconId ID of connected beacon to fetch settings from.
     * @param callbackFn Executed upon successful execution.
     */
    public void getBeaconSettings(int beaconId, Callback<BeaconSettings> callbackFn) {

        // TODO make threaded (currently blocks startup)
        HttpHandler f = new HttpHandler(this.context);

        try {

            f.execute(
                    new HttpHandlerParams(
                            "http://192.168.1.100/beacon/" + beaconId + "/settings",
                            HttpHandlerMethod.GET,
                            new HttpHeader[]{new HttpHeader(MainActivity.HEADER_KEY_AUTH, MainActivity.HEADER_VALUE_AUTH)},
                            null
                    )
            );

            HttpHandlerResult res = f.get();

            Log.d(LOGGING_TAG, "Fetched beacon settings from remote successfully.");

            callbackFn.call(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(res.getResponseValue(), BeaconSettings.class));

        }catch (Throwable t) {
            Log.e(LOGGING_TAG, t.getMessage(), t);
            callbackFn.error(t);
        }

    }

    public List<String> getAllowedDeviceAddresses() throws RuntimeException {

        if (this.beacons == null) {
            throw new RuntimeException("Can't get allowed device addresses, fetch device list beforehand.");
        }

        List<String> allowedBluetoothAddresses = new ArrayList<String>();
        for (Beacon b : beacons) {
            allowedBluetoothAddresses.add(b.getBluetoothAddress());
        }

        return allowedBluetoothAddresses;

    }

    /**
     * TODO
     * @param lookupAddress Address to look for.
     * @return Matching beacon or null if none is found.
     */
    public Beacon getBeaconByAddress(String lookupAddress) {

        for (Beacon b : this.beacons) {
            if (b.getBluetoothAddress().equals(lookupAddress)) {
                return b;
            }
        }

        return null;

    }

}
