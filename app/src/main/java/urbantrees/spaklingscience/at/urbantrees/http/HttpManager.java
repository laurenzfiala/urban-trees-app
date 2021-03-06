package urbantrees.spaklingscience.at.urbantrees.http;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommand;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponseType;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconReadoutResult;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
import urbantrees.spaklingscience.at.urbantrees.util.BeaconLogger;
import urbantrees.spaklingscience.at.urbantrees.util.Callback;
import urbantrees.spaklingscience.at.urbantrees.util.HasContext;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

/**
 * Contains http request methods and holds some app objects.
 * @author Laurenz Fiala
 * @since 2018/10/27
 */
public class HttpManager extends HasContext {

    private static final String LOGGING_TAG = HttpManager.class.getName();

    private static final String JWT_AUTH_HEADER_KEY = "Authorization";
    private static final String APIKEY_AUTH_HEADER_KEY = "x-api-key";

    private static Map<String, String> headers = new HashMap<>();

    private Beacon[] beacons;

    public HttpManager(Activity context, ApplicationProperties props) {
        super(context, props);
    }

    /**
     * HTTP-GET all working beacons to filter for.
     * @param callbackFn Executed upon successful execution.
     */
    public void fetchDeviceList(Callback<Void> callbackFn) {

        HttpHandler f = new HttpHandler();

        try {

            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.list.url"),
                            HttpHandlerMethod.GET,
                            getHttpHeaders(),
                            null
                    )
            );

            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

            Log.d(LOGGING_TAG, "Fetched beacon list from remote successfully.");

            this.beacons = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(res.getResponseValue(), Beacon[].class);

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

        HttpHandler f = new HttpHandler();

        try {

            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.settings.url", beaconId),
                            HttpHandlerMethod.GET,
                            getHttpHeaders(),
                            null
                    )
            );

            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

            Log.d(LOGGING_TAG, "Fetched beacon settings from remote successfully.");

            BeaconSettings settings = new ObjectMapper()
                    .setDateFormat(new SimpleDateFormat(this.props.getProperty("date.format")))
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(res.getResponseValue(), BeaconSettings.class);

            callbackFn.call(settings);

        } catch (Throwable t) {
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

    public void sendBeaconResult(BluetoothDevice device, UARTLogEntry[] logs, UARTCommand settingsCmd, UARTCommand telemetricsCmd, Callback callback) {

        if (logs == null || logs.length == 0) {
            Log.i(LOGGING_TAG, "No datasets received from beacon. Not uploading any.");
            callback.call(null);
            return;
        }

        try {

            BeaconSettings settings = this.getBeaconSettings(device.getBeacon(), settingsCmd, telemetricsCmd);
            long timeSinceDataReadoutMs = System.currentTimeMillis() - device.getDataReadoutTime();
            BeaconReadoutResult result = new BeaconReadoutResult(logs, settings, timeSinceDataReadoutMs);

            DateFormat df = new SimpleDateFormat(
                    this.props.getProperty("date.format"),
                    Locale.US
            );
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String payload = new ObjectMapper()
                    .setDateFormat(df)
                    .writeValueAsString(result);

            HttpHandler f = new HttpHandler();
            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.datatransfer.url", device.getBeacon().getId()),
                            HttpHandlerMethod.PUT,
                            headers,
                            payload
                    )
            );
            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

            callback.call(null);

        } catch (Throwable t) {
            callback.error(t);
        }

    }

    public BeaconSettings getBeaconSettings(Beacon beacon, UARTCommand settingsCmd, UARTCommand telemetricsCmd) {

        if (settingsCmd.getResponses().size() == 0) {
            BeaconLogger.error(beacon, "Can't send beacon settings, settings command info missing.");
            throw new RuntimeException("Can't send beacon settings, settings command info missing.");
        }
        if (telemetricsCmd.getResponses().size() == 0) {
            BeaconLogger.error(beacon, "Can't send beacon settings, telemetrics command info missing.");
            throw new RuntimeException("Can't send beacon settings, telemetrics command info missing.");
        }

        BeaconSettings newSettings = new BeaconSettings();
        newSettings.setDeviceName(settingsCmd.<String>findResponse(UARTResponseType.DEVICE_NAME).getValue());
        newSettings.setFirmwareVersionCode(settingsCmd.<Integer>findResponse(UARTResponseType.DEVICE_VERSION).getValue());
        newSettings.setTransmitPowerDb(settingsCmd.<Integer>findResponse(UARTResponseType.TRANSMISSION_STRENGTH).getValue());
        newSettings.setBatteryLevel(settingsCmd.<Integer>findResponse(UARTResponseType.BATTERY_LEVEL).getValue());
        newSettings.setTemperatureUnits(settingsCmd.<String>findResponse(UARTResponseType.TEMPERATURE_UNITS).getValue());
        newSettings.setMemoryCapacity(settingsCmd.<Integer>findResponse(UARTResponseType.MEMORY_CAPACITY).getValue());
        newSettings.setRefTime(settingsCmd.<Date>findResponse(UARTResponseType.REFERENCE_DATE).getValue());
        newSettings.setDeviceId(settingsCmd.<Integer>findResponse(UARTResponseType.ID).getValue());
        newSettings.setPhysicalButtonEnabled(settingsCmd.<Boolean>findResponse(UARTResponseType.PHYSICAL_BUTTON_ENABLED).getValue());
        newSettings.setTemperatureCalibration(settingsCmd.<Double>findResponse(UARTResponseType.TEMPERATURE_CALIBRATION).getValue());
        newSettings.setHumidityCalibration(settingsCmd.<Integer>findResponse(UARTResponseType.HUMIDITY_CALIBRATION).getValue());
        newSettings.setLoggingIntervalSec(telemetricsCmd.<Integer>findResponse(UARTResponseType.LOG_FREQUENCY).getValue());
        newSettings.setSensorIntervalSec(telemetricsCmd.<Integer>findResponse(UARTResponseType.SENSOR_FREQUENCY).getValue());
        newSettings.setAdvertisingFrequencyMs(0); // TODO
        newSettings.setPin(beacon.getSettings().getPin());
        newSettings.setCheckDate(new Date());

        return newSettings;

    }

    public void setJWTToken(String token) {
        if (token == null) {
            return;
        }
        headers.put(JWT_AUTH_HEADER_KEY, token);
    }

    public void unsetJWTToken() {
        this.headers.remove(JWT_AUTH_HEADER_KEY);
    }

    public boolean isJWTTokenAuthenticated() {
        return headers.containsKey(JWT_AUTH_HEADER_KEY);
    }

    public void setApiKeyToken(String token) {
        headers.put(APIKEY_AUTH_HEADER_KEY, token);
    }

    public Beacon[] getBeacons() {
        return beacons;
    }

    public static HttpHeader[] getHttpHeaders() {
        return Utils.mapToHttpHeaders(headers);
    }

}

