package urbantrees.spaklingscience.at.urbantrees.http;

import android.app.Activity;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTCommand;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTLogEntry;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTManager;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponse;
import urbantrees.spaklingscience.at.urbantrees.bluetooth.UARTResponseType;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconSettings;
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

    private Map<String, String> headers = new HashMap<>();

    private Beacon[] beacons;

    public HttpManager(Activity context , ApplicationProperties props) {
        super(context, props);
    }

    /**
     * HTTP-GET all working beacons to filter for.
     * @param callbackFn Executed upon successful execution.
     */
    public void fetchDeviceList(Callback<Void> callbackFn) {

        HttpHandler f = new HttpHandler(this.context);

        try {

            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.list.url"),
                            HttpHandlerMethod.GET,
                            Utils.mapToHttpHeaders(this.headers),
                            null
                    )
            );

            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

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

        HttpHandler f = new HttpHandler(this.context);

        try {

            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.settings.url", beaconId),
                            HttpHandlerMethod.GET,
                            Utils.mapToHttpHeaders(this.headers),
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

            callbackFn
                .call(settings);

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

    public void sendBeaconData(Beacon beacon, UARTLogEntry[] logs, Callback callback) {

        List<Object> datasets = new ArrayList<Object>();
        for (UARTLogEntry entry : logs) {
            datasets.add(entry);
        }

        if (datasets.size() == 0) {
            Log.i(LOGGING_TAG, "No datasets received from beacon. Not uploading any.");
            callback.call(null);
            return;
        }

        try {
            DateFormat df = new SimpleDateFormat(this.props.getProperty("date.format"));
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String test = new ObjectMapper()
                    .setDateFormat(df)
                    .writeValueAsString(datasets);

            HttpHandler f = new HttpHandler(this.context);
            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.datatransfer.url", beacon.getId()),
                            HttpHandlerMethod.PUT,
                            headers,
                            test
                    )
            );
            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

            callback.call(null);

        } catch (Throwable t) {
            callback.error(t);
        }

    }

    // TODO add to flow
    public void sendBeaconSettings(Beacon beacon, UARTCommand settingsCmd, UARTCommand telemetricsCmd, Callback callback) {

        if (settingsCmd.getResponses().size() == 0) {
            Log.i(LOGGING_TAG, "Settings have not been received.");
            callback.call(null);
            return;
        }
        if (telemetricsCmd.getResponses().size() == 0) {
            Log.i(LOGGING_TAG, "Telemetrics have not been received.");
            callback.call(null);
            return;
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
        newSettings.setLoggingIntervalMin(telemetricsCmd.<Integer>findResponse(UARTResponseType.LOG_FREQUENCY).getValue());
        newSettings.setSensorIntervalSec(telemetricsCmd.<Integer>findResponse(UARTResponseType.SENSOR_FREQUENCY).getValue());
        newSettings.setAdvertisingFrequencyMs(0); // TODO
        newSettings.setPin(beacon.getSettings().getPin());
        newSettings.setCheckDate(new Date());

        try {
            DateFormat df = new SimpleDateFormat(this.props.getProperty("date.format"));
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String newSettingsPayload = new ObjectMapper()
                    .setDateFormat(df)
                    .writeValueAsString(newSettings); // TODO move to config

            HttpHandler f = new HttpHandler(this.context);
            f.execute(
                    new HttpHandlerParams(
                            this.props.getProperty("beacon.settings.url", beacon.getId()),
                            HttpHandlerMethod.PUT,
                            headers,
                            newSettingsPayload
                    )
            );
            HttpHandlerResult res = f.get();
            HttpHandlerResult.isSuccessfulElseThrow(res);

            callback.call(null);
        } catch (Throwable t) {
            callback.error(t);
        }

    }

    public void setJWTToken(String token) {
        if (token == null) {
            return;
        }
        this.headers.put(JWT_AUTH_HEADER_KEY, token);
    }

    public void unsetJWTToken() {
        this.headers.remove(JWT_AUTH_HEADER_KEY);
    }

    public boolean isJWTTokenAuthenticated() {
        return this.headers.containsKey(JWT_AUTH_HEADER_KEY);
    }

    public void setApiKeyToken(String token) {
        this.headers.put(APIKEY_AUTH_HEADER_KEY, token);
    }

    public Beacon[] getBeacons() {
        return beacons;
    }

}

