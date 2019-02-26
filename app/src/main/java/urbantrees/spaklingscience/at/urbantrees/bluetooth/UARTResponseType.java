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

import urbantrees.spaklingscience.at.urbantrees.util.ByteUtils;

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
                Log.w(this.getClass().getName(), "Reference Date is not set, using current date.");
                return new UARTResponse<Date>(this, new Date());
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


            final byte[] lastChar = pkg.getCharacteristic(pkg.getCharacteristics().length - 1);
            final byte[] trimmedVal = ByteUtils.trim(lastChar);
            final String stringValue = ByteUtils.toString(trimmedVal, UARTCommand.ENCODING);

            if (stringValue.endsWith(".")) {
                return 1;
            }
            return -1;

        }

        @Override
        public UARTResponse<UARTLogEntry[]> getResponse(final UARTResponsePackage pkg) throws Throwable {

            Integer numLogs = pkg.getPreviousCommands().<Integer>findResponse(UARTResponseType.CURRENT_NUM_LOGS).getValue();
            Long refDate = pkg.getPreviousCommands().<Date>findResponse(UARTResponseType.REFERENCE_DATE).getValue().getTime();
            Integer logFreq = pkg.getPreviousCommands().<Integer>findResponse(UARTResponseType.LOG_FREQUENCY).getValue();

            if (numLogs == null || refDate == null || logFreq == null) {
                throw new RuntimeException("The response type '" + this + "' needs the log amount, reference date and log frequency to be retrieved beforehand.");
            }

            List<UARTLogEntry> entries = new ArrayList<UARTLogEntry>();

            final byte[][] chars = pkg.getCharacteristics();
            Double[][] vals = new Double[3][numLogs]; // log amount read-out may be outdated, so add buffer
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

                    final double val = (double) ByteUtils.octalToDecimal(Arrays.copyOfRange(chars[i], j, j+2)) / 10d;
                    try {
                        vals[valIndex][valMetricIndex] = val;

                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("", "");
                    }
                    valMetricIndex++;

                }

            }

            // TODO
            // make arrays lists
            // dont use log amount for array size
            // dont use rest of interval, but use ref date + log amount * interval

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            int offsetToLastLog = (int) (cal.getTimeInMillis() - refDate) % (logFreq * 1000);
            cal.add(Calendar.MILLISECOND, -offsetToLastLog);

            for (int i = vals[0].length - 1; i > 0; i--) {

                if (vals[0][i] == null || vals[1][i] == null || vals[2][i] == null) {
                    continue;
                }

                entries.add(
                        new UARTLogEntry(
                                cal.getTime(),
                                vals[0][i],
                                vals[1][i],
                                vals[2][i]
                        )
                );
                cal.add(Calendar.SECOND, -logFreq);

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
