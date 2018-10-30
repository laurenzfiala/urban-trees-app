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

    private String apiKeyHeaderKey;
    private String apiKeyHeaderValue;

    private String deviceListUrl;
    private String beaconSettingsUrl;

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
                            this.deviceListUrl,
                            HttpHandlerMethod.GET,
                            new HttpHeader[]{new HttpHeader(this.apiKeyHeaderKey, this.apiKeyHeaderValue)},
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
    public void getBeaconSettings(int beaconId, Callback<Beacon> callbackFn) {

        // TODO make threaded (currently blocks startup)
        HttpHandler f = new HttpHandler(this.context);

        try {

            f.execute(
                    new HttpHandlerParams(
                            this.beaconSettingsUrl.replaceAll("\\{beaconId\\}", beaconId + ""),
                            HttpHandlerMethod.GET,
                            new HttpHeader[]{new HttpHeader(this.apiKeyHeaderKey, this.apiKeyHeaderValue)},
                            null
                    )
            );

            HttpHandlerResult res = f.get();

            Log.d(LOGGING_TAG, "Fetched beacon settings from remote successfully.");

            BeaconSettings settings = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(res.getResponseValue(), BeaconSettings.class);

            Beacon beacon = null;
            for (Beacon b : this.beacons) {
                if (b.getId() == beaconId) {
                    beacon = b;
                    break;
                }
            }

            if (beacon == null) {
                Log.e(LOGGING_TAG, "Beacon with ID " + beaconId + " not found in beacon list.");
                callbackFn.error(new Throwable("Beacon settings could not be associated with beacon."));
            }

            beacon.setSettings(settings);

            callbackFn
                .call(beacon);

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

    public String getApiKeyHeaderKey() {
        return apiKeyHeaderKey;
    }

    public void setApiKeyHeaderKey(String apiKeyHeaderKey) {
        this.apiKeyHeaderKey = apiKeyHeaderKey;
    }

    public String getApiKeyHeaderValue() {
        return apiKeyHeaderValue;
    }

    public void setApiKeyHeaderValue(String apiKeyHeaderValue) {
        this.apiKeyHeaderValue = apiKeyHeaderValue;
    }

    public String getDeviceListUrl() {
        return deviceListUrl;
    }

    public void setDeviceListUrl(String deviceListUrl) {
        this.deviceListUrl = deviceListUrl;
    }

    public String getBeaconSettingsUrl() {
        return beaconSettingsUrl;
    }

    public void setBeaconSettingsUrl(String beaconSettingsUrl) {
        this.beaconSettingsUrl = beaconSettingsUrl;
    }

}

