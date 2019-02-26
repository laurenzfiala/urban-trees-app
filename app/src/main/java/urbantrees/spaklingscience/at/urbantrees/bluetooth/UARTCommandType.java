package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * Holds all command
 * @author Laurenz Fiala
 * @since 2019/02/24
 */
public enum UARTCommandType implements UARTCommandTypeInterface {

    LOCK_CHECK_COMMAND {
        @Override
        public UARTCommand getCommand(String ...args) {
            return new UARTCommand(
                    this,
                    "*batt",
                    UARTResponseStrategy.ORDERED_STRICT,
                    UARTResponseType.NON_EMPTY_NON_ERROR
            );
        }
    },
    SETTINGS_COMMAND {
        @Override
        public UARTCommand getCommand(String ...args) {
            return new UARTCommand(
                    this,
                    "*info",
                    UARTResponseStrategy.SKIP_UNMATCHED,
                    UARTResponseType.DEVICE_NAME,
                    UARTResponseType.DEVICE_VERSION,
                    UARTResponseType.TRANSMISSION_STRENGTH,
                    UARTResponseType.BATTERY_LEVEL,
                    UARTResponseType.TEMPERATURE_UNITS,
                    UARTResponseType.MEMORY_CAPACITY,
                    UARTResponseType.REFERENCE_DATE,
                    UARTResponseType.ID,
                    UARTResponseType.PHYSICAL_BUTTON_ENABLED,
                    UARTResponseType.TEMPERATURE_CALIBRATION,
                    UARTResponseType.HUMIDITY_CALIBRATION
            );
        }
    },
    TELEMETRICS_COMMAND {
        @Override
        public UARTCommand getCommand(String ...args) {
            return new UARTCommand(
                    this,
                    "*tell",
                    UARTResponseStrategy.SKIP_UNMATCHED,
                    UARTResponseType.SENSOR_FREQUENCY,
                    UARTResponseType.LOG_FREQUENCY,
                    UARTResponseType.CURRENT_NUM_LOGS
            );
        }
    },
    LOGGER_COMMAND {
        @Override
        public UARTCommand getCommand(String ...args) {
            return new UARTCommand(
                    this,
                    "*logall",
                    UARTResponseStrategy.ORDERED_STRICT_REUSE_LAST,
                    UARTResponseType.LOG_ENTRY
            );
        }
    },
    LOCK_UNLOCK_COMMAND {
        @Override
        public UARTCommand getCommand(String ...args) {
            if (args == null || args.length != 1) {
                throw new RuntimeException("Invalid arguments");
            }
            return new UARTCommand(
                    this,
                    "*pwd" + args[0],
                    UARTResponseStrategy.SKIP_UNMATCHED
            );
        }
    }

}
