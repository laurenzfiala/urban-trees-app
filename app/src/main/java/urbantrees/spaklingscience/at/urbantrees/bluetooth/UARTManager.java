package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import urbantrees.spaklingscience.at.urbantrees.R;
import urbantrees.spaklingscience.at.urbantrees.entities.Beacon;
import urbantrees.spaklingscience.at.urbantrees.util.Dialogs;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeEmitter;
import urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeType;

/**
 * High level abstraction layer of the standard {@link android.bluetooth.BluetoothGatt}
 * handling and associated logic.
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class UARTManager extends PropertyChangeEmitter implements PropertyChangeListener {

    private static final String LOGGING_TAG = UARTManager.class.getName();

    /**
     * TODO
     */
    private UARTCallbackHandler callbackHandler;

    /**
     * FIFO double-ended queue holding all commands to send via the UART interface.
     * It is a dequeue, because we need to be able to add a command dynamically in front of the
     * succeeding ones.
     */
    private Deque<UARTCommand> commandQueue = new LinkedBlockingDeque<UARTCommand>();

    /**
     * TODO
     */
    private UARTCommandList successfulCommands;

    /**
     * TODO
     */
    private BluetoothGatt gatt;

    private BluetoothCoordinator bluetoothCoordinator;

    private urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice currentDevice;

    private Beacon currentBeacon;

    private static final int COMMAND_TRIES = 3;

    /**
     * Whether the latest command has exceeded its
     * retry count or not.
     */
    private boolean commandTriesExceeded = false;

    public static final UARTCommand LOCK_CHECK_COMMAND = new UARTCommand(
            "*batt",
            UARTResponseStrategy.ORDERED_STRICT,
            UARTResponseType.NON_EMPTY_NON_ERROR
    );

    public static final UARTCommand SETTINGS_COMMAND = new UARTCommand(
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
    public static final UARTCommand TELEMETRICS_COMMAND = new UARTCommand(
            "*tell",
            UARTResponseStrategy.SKIP_UNMATCHED,
            UARTResponseType.SENSOR_FREQUENCY,
            UARTResponseType.LOG_FREQUENCY,
            UARTResponseType.CURRENT_NUM_LOGS
    );
    public static final UARTCommand LOGGER_COMMAND = new UARTCommand(
            "*logall",
            UARTResponseStrategy.ORDERED_STRICT_REUSE_LAST,
            UARTResponseType.LOG_ENTRY
    );

    public UARTManager(Activity context) {
        super(context);

        this.bluetoothCoordinator = new BluetoothCoordinator(context);
        this.bluetoothCoordinator.listen(PropertyChangeType.BLUETOOTH_DEVICE, this);
        this.bluetoothCoordinator.listen(PropertyChangeType.THROWABLE, this);
    }

    public boolean enableBluetooth() {

        return this.bluetoothCoordinator.enableBluetooth();

    }

    public void fetchDeviceInfo(List<String> allowedBluetoothAdresses) {

        this.bluetoothCoordinator.scanForDevices(allowedBluetoothAdresses);

    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if (PropertyChangeType.equals(propertyChangeEvent.getPropertyName(), PropertyChangeType.BLUETOOTH_DEVICE)) {

            this.onBluetoothDeviceFound(PropertyChangeType.BLUETOOTH_DEVICE.cast(propertyChangeEvent.getNewValue()));
            this.successfulCommands = new UARTCommandList();

        } else if (PropertyChangeType.equals(propertyChangeEvent.getPropertyName(), PropertyChangeType.GATT_CHARACTERISTIC)) {

            final BluetoothGattCharacteristic characteristic = PropertyChangeType.GATT_CHARACTERISTIC.cast(propertyChangeEvent.getNewValue());
            this.onDataRead(characteristic);

        } else if (PropertyChangeType.equals(propertyChangeEvent.getPropertyName(), PropertyChangeType.GATT_STATUS)) {

            final int gattStatus = PropertyChangeType.GATT_STATUS.cast(propertyChangeEvent.getNewValue());
            if (gattStatus == BluetoothGatt.STATE_DISCONNECTED) {
                this.onBluetoothDeviceDisconnected();
            }

        } else if (PropertyChangeType.equals(propertyChangeEvent.getPropertyName(), PropertyChangeType.THROWABLE)) {

            final Throwable t = PropertyChangeType.THROWABLE.cast(propertyChangeEvent.getNewValue());
            Log.e(LOGGING_TAG, t.getMessage());
            this.notify(PropertyChangeType.THROWABLE, t);

        }

    }

    private void onDataRead(final BluetoothGattCharacteristic characteristic) {

        Log.d(LOGGING_TAG, "Reading data from device '" + this.currentDevice + "'...");
        this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_INFO_PART_FETCHED);

        try {
            this.getCurrentCommand().addResponse(characteristic, this.successfulCommands);
        } catch (Throwable t) {
            Log.e(LOGGING_TAG, "Exception while adding response to command '" + this.getCurrentCommand() + "': " + t.getMessage());
            this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_INFO_FETCH_FAILED);
        }

    }

    private void onBluetoothDeviceDisconnected() {

        // TODO
        //
        // handle case when nearest device is replaced by other device *during command execution*

        this.gatt.close();

        if (this.getCurrentCommand() == null) {
            Log.d(LOGGING_TAG, "onBluetoothDeviceDisconnected() - Command is null");
            return;
        }

        if (this.getCurrentCommand() == LOCK_CHECK_COMMAND && this.getCurrentCommand().getResponses().size() > 0 && (boolean) this.getCurrentCommand().getResponses().get(0).getValue() == false) {
            this.finalizeCommand();
            Log.d(LOGGING_TAG, "Device seems to be locked, unlocking now.");
            this.commandQueue.addFirst(
                    new UARTCommand(
                            "*pwd" + this.getCurrentBeacon().getSettings().getPin(),
                            UARTResponseStrategy.SKIP_UNMATCHED
                    )
            );
            this.connectAndExecuteCommand();
        } else if (this.getCurrentCommand().isPotentiallyDone()) {
            Log.d(LOGGING_TAG, "Command '" + this.getCurrentCommand() + "' on device '" + this.currentDevice + "' execution done.");
            this.successfulCommands.add(this.finalizeCommand());
            if (this.commandQueue.size() == 0) {
                this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_INFO_FETCHED);
                if (this.commandTriesExceeded) {
                    //Dialogs.progressSnackbar(this.context.findViewById(R.id.layout_root), this.context.getString(R.string.beacon_data_get_failed));
                    this.bluetoothCoordinator.resetNearestDevice();
                }
            } else {
                this.connectAndExecuteCommand();
            }
        } else {
            Log.e(LOGGING_TAG, "BLE device was disconnected before command '" + this.getCurrentCommand() + "' could be executed.");

            int tries = this.getCurrentCommand().getTries();
            if (tries >= COMMAND_TRIES) {
                this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_INFO_FETCH_FAILED);
                Log.e(LOGGING_TAG, "Command '" + this.getCurrentCommand() + "' could not be executed after " + tries + " tries. Cancelling communication with device and retrying BLE connection.");
                this.commandQueue.clear();
                this.addLockCommand();
                this.commandTriesExceeded = true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            this.connectAndExecuteCommand();
        }

    }

    private void addLockCommand() {

        this.commandQueue.offer(
            new UARTCommand(
                    "*pwd" + this.getCurrentBeacon().getSettings().getPin(),
                    UARTResponseStrategy.SKIP_UNMATCHED
            )
        );

    }

    private void onBluetoothDeviceFound(urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice bluetoothDevice) {

        // TODO only if current device is done
        this.currentDevice = bluetoothDevice;
        this.callbackHandler = null;

        Log.i(LOGGING_TAG, "Bluetooth device found: " + bluetoothDevice.getAddress());
        this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_FOUND);
        this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_INFO_FETCH);

    }

    private void onBluetoothError(Throwable t) {
        Log.e(LOGGING_TAG, "BLE device was disconnected before command, exception when finding devices: " + t.getMessage());
        this.notify(PropertyChangeType.UART_MANAGER_STATUS, UARTManagerStatus.DEVICE_CONNECTION_FAILED);
    }

    public void populateCommands() {

        this.commandQueue.offer(LOCK_CHECK_COMMAND);
        this.commandQueue.offer(TELEMETRICS_COMMAND);
        this.commandQueue.offer(SETTINGS_COMMAND);
        this.commandQueue.offer(LOGGER_COMMAND);

        this.addLockCommand();

    }

    public void connectAndExecuteCommand() {

        if (this.callbackHandler == null) {
            this.callbackHandler = new UARTCallbackHandler(this.context, this);
            this.callbackHandler.getPropertyEmitter().listen(PropertyChangeType.GATT_CHARACTERISTIC, this);
            this.callbackHandler.getPropertyEmitter().listen(PropertyChangeType.GATT_STATUS, this);
        }
        this.callbackHandler.setDevice(new UARTDevice(this.currentDevice));

        this.gatt = this.getCurrentDevice().getDevice().connectGatt(this.context, false, this.callbackHandler);
        boolean test = this.gatt.connect();
        Log.i("", "");

    }

    public UARTCommand finalizeCommand() {
        return this.commandQueue.poll();
    }

    public UARTCommand getCurrentCommand() {
        return this.commandQueue.peek();
    }

    public urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice getCurrentDevice() {
        if (currentDevice == null) {
            throw new RuntimeException(""); // TODO
        }
        return currentDevice;
    }

    public Beacon getCurrentBeacon() {
        return currentBeacon;
    }

    public void setCurrentBeacon(Beacon currentBeacon) {
        this.currentBeacon = currentBeacon;
    }
}
