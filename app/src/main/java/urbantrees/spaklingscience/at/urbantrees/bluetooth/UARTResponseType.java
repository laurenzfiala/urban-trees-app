package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.util.Log;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import urbantrees.spaklingscience.at.urbantrees.entities.BeaconStatus;
import urbantrees.spaklingscience.at.urbantrees.util.BeaconLogger;
import urbantrees.spaklingscience.at.urbantrees.util.ByteUtils;
import urbantrees.spaklingscience.at.urbantrees.util.Utils;

/**
 * TODO
 * Holds all needed response types used in {@link UARTResponse}
 * like "battery level".
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public enum UARTResponseType implements UARTResponseTypeInterface {

    WILDCARD {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return -1; }

        @Override
        public UARTResponse<Boolean> getResponse(final UARTResponsePackage pkg) {
            return new UARTResponse<Boolean>(this, true);
        }
    },
    NON_EMPTY_NON_ERROR {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Boolean> getResponse(final UARTResponsePackage pkg) {
            final String val = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            return new UARTResponse<Boolean>(this, !("".equals(val) || "Error".equals(val)));
        }
    },
    DEVICE_NAME {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<String> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Name:\\s+(.*)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<String>(this, stringValue);
        }
    },
    DEVICE_VERSION {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Ver no: (\\d+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    TRANSMISSION_STRENGTH {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Txp lvl: (-?\\d+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    BATTERY_LEVEL {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Batt lvl: (\\d{2,3})%", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    TEMPERATURE_UNITS {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<String> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Units: ([CF])", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<String>(this, stringValue);
        }
    },
    MEMORY_CAPACITY {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Mem: (\\d+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    REFERENCE_DATE {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 2; }

        @Override
        public UARTResponse<Date> getResponse(final UARTResponsePackage pkg) throws Throwable {

            if (!ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING).equals("Date yymmddhhmm:")) {
                return null;
            }

            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(1)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("-> (.*)", stringValue);

            if ("00:00:00:00:00".equals(stringValue)) {
                Log.d(this.getClass().getName(), "Reference Date is not set.");
                return new UARTResponse<Date>(this, null);
            }

            DateFormat dateFormat = new SimpleDateFormat("yy:MM:dd:HH:mm");
            Date deviceCurrentDate = dateFormat.parse(stringValue);

            return new UARTResponse<Date>(this, deviceCurrentDate);
        }
    },
    ID {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Id: (\\d+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    PHYSICAL_BUTTON_ENABLED {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Boolean> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Btn on/off: (\\d)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Boolean>(this, Integer.parseInt(stringValue) == 0);
        }
    },
    TEMPERATURE_CALIBRATION {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Double> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Temp Cal. ([-+]?[0-9]*\\.?[0-9]+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Double>(this, Double.parseDouble(stringValue));
        }
    },
    HUMIDITY_CALIBRATION {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Hum Calx10 ([-+]?\\d+)%", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    SENSOR_FREQUENCY {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Snsr Frq: (\\d+)s", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    LOG_FREQUENCY {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("Log Frq: (\\d+)s", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    CURRENT_NUM_LOGS {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<Integer> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            stringValue = this.findFirstGroup("No. logs: (\\d+)", stringValue);

            if (stringValue == null) {
                return null;
            }
            return new UARTResponse<Integer>(this, Integer.parseInt(stringValue));
        }
    },
    OK {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return -1; }

        @Override
        public UARTResponse<Boolean> getResponse(final UARTResponsePackage pkg) {
            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            return new UARTResponse<Boolean>(this, "OK".equals(stringValue));
        }
    },
    LOG_ENTRY {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) throws Throwable {

            int logAmount = (int) pkg.getPreviousCommands().findResponse(UARTResponseType.CURRENT_NUM_LOGS).getValue();

            if (logAmount <= 0) {
                Log.i(this.name(), "There are no logs stored on the device '" + this + "' needs the number of logs-response to be executed beforehand.");
                return 0;
            }

            final byte[] trimmedLastChar = ByteUtils.trim(pkg.getCharacteristic(pkg.getCharacteristics().length - 1));
            int fromIndex = trimmedLastChar.length-2;
            if (trimmedLastChar.length % 2 != 0) { // steps of 2, if uneven only check last byte
                fromIndex++;
            }
            final byte[] val = Arrays.copyOfRange(trimmedLastChar, fromIndex, trimmedLastChar.length);
            final String stringValue = ByteUtils.toString(val, UARTCommand.ENCODING);

            if (stringValue.equals(".")) {
                return 1;
            }
            return -1;

        }

        @Override
        public UARTResponse<UARTLogEntry[]> getResponse(final UARTResponsePackage pkg) throws Throwable {

            pkg.getAssocDevice().setDataReadoutTime(System.currentTimeMillis());

            Integer numLogs = pkg.getPreviousCommands().<Integer>findResponse(UARTResponseType.CURRENT_NUM_LOGS).getValue();
            Integer logFreq = pkg.getPreviousCommands().<Integer>findResponse(UARTResponseType.LOG_FREQUENCY).getValue();
            UARTResponse<Date> refDateResponse = pkg.getPreviousCommands().<Date>findResponse(UARTResponseType.REFERENCE_DATE);

            Date refDate = refDateResponse.getValue();
            if (refDate == null) { // fallback to ref date from adv pkg (bug in blue maestro beacons)
                refDate = Utils.geAdvPkgRefDate(pkg.getAssocDevice());
                refDateResponse.setValue(refDate); // update value for settings transfer
            }
            if (refDate == null) {
                BeaconLogger.error(pkg.getAssocDevice(), "Reference date is not set, cancelling command execution.");
                throw new RuntimeException("Reference date must be set before command '" + this + "' can be executed.");
            }
            if (numLogs == null || logFreq == null) {
                throw new RuntimeException("The response type '" + this + "' needs the log amount and log frequency to be retrieved beforehand.");
            }

            final byte[][] chars = pkg.getCharacteristics();
            Double[][] vals = new Double[3][Math.min(numLogs+5, 6000)]; // numLogs+5 because it may be outdated
            int valIndex = 0, valMetricIndex = 0;

            charLoop:
            for (int i = 0; i < chars.length; i++) {

                for (int j = 0; j < chars[i].length; j += 2) {
                    final byte[] trimmedVal = ByteUtils.trim(Arrays.copyOfRange(chars[i], j, j+2));
                    final String stringValue = ByteUtils.toString(trimmedVal, UARTCommand.ENCODING);
                    if (stringValue.endsWith(",,")) {
                        valIndex++;
                        valMetricIndex = 0;
                        break;
                    } else if (stringValue.equals(".")) {
                        break charLoop;
                    }

                    final double val = (double) ByteUtils.twosComplementToDecimal(Arrays.copyOfRange(chars[i], j, j+2)) / 10d;
                    try {
                        vals[valIndex][valMetricIndex] = val;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("", "");
                    }
                    valMetricIndex++;

                }

            }

            /* old impl - replaced because it is very dependant on timezones
            *  we now calculate the timestamps on the backend */
            // TODO comment out
            long logDate = refDate.getTime();
            if (numLogs > 6000) {
                logDate += ((long) logFreq * 1000l) * ((long) numLogs - 6000l);
            }

            List<UARTLogEntry> entries = new ArrayList<UARTLogEntry>();
            for (int i = 0; i < vals[0].length; i++) {

                if (vals[0][i] == null && vals[1][i] == null && vals[2][i] == null) {
                    break;
                } else if (vals[0][i] == null || vals[1][i] == null || vals[2][i] == null) {
                    BeaconLogger.error(pkg.getAssocDevice(), "Temp/Humi/Dew do not have equal amounts of datapoints stored.");
                    throw new RuntimeException("Temp/Humi/Dew do not have equal amounts of datapoints stored.");
                }

                entries.add(
                        new UARTLogEntry(
                                new Date(logDate),
                                vals[0][i],
                                vals[1][i],
                                vals[2][i]
                        )
                );
                logDate += (logFreq * 1000);

            }

            return new UARTResponse<UARTLogEntry[]>(this, entries.toArray(new UARTLogEntry[entries.size()]));
        }
    },
    LOG_STREAM_ENTRY {

        @Override
        public int getResponseAmount(UARTResponsePackage pkg) { return 1; }

        @Override
        public UARTResponse<UARTLogEntry> getResponse(final UARTResponsePackage pkg) throws Throwable {

            Long refDate = pkg.getPreviousCommands().<Date>findResponse(UARTResponseType.REFERENCE_DATE).getValue().getTime();
            Integer logFreq = pkg.getPreviousCommands().<Integer>findResponse(UARTResponseType.LOG_FREQUENCY).getValue();

            if (refDate == null || logFreq == null) {
                throw new RuntimeException("The response type '" + this + "' needs the reference date and log frequency to be executed beforehand.");
            }

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(refDate);
            cal.add(Calendar.SECOND, logFreq * pkg.getMatchedResponseAmount());

            String stringValue = ByteUtils.toString(ByteUtils.trim(pkg.getCharacteristic(0)), UARTCommand.ENCODING);
            String[] thd = this.findGroups("T([-+]?[0-9]*\\.?[0-9]+)H([-+]?[0-9]*\\.?[0-9]+)D([-+]?[0-9]*\\.?[0-9]+)", stringValue);

            if (thd == null || thd.length != 3) {
                return null;
            }
            return new UARTResponse<UARTLogEntry>(this,
                new UARTLogEntry(cal.getTime(), Double.parseDouble(thd[0]), Double.parseDouble(thd[1]), Double.parseDouble(thd[2]))
            );
        }
    };

    /**
     * TODO
     * @param regex
     * @param input
     * @return the first group of the given regex pattern if found; null if not found
     */
    protected String[] findGroups(final String regex, final String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return null;
        }
        String[] groups = new String[matcher.groupCount()];
        for (int i = 0; i < matcher.groupCount(); i++) {
            groups[i] = matcher.group(i+1);
        }
        return groups;
    }

    /**
     * TODO
     * @param regex
     * @param input
     * @return the first group of the given regex pattern if found; null if not found
     */
    protected String findFirstGroup(final String regex, final String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

}
