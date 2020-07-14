package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.entities.BeaconStatus;
import urbantrees.spaklingscience.at.urbantrees.util.BeaconLogger;
import urbantrees.spaklingscience.at.urbantrees.util.HasContext;

/**
 * High level abstraction layer of the standard {@link android.bluetooth.BluetoothGatt}
 * handling and associated logic.
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class UARTManager extends HasContext implements UARTCallbackHandler.OnUARTCallbackHandlerChange {

    private static final String LOGGING_TAG = UARTManager.class.getName();

    /**
     * PIN used to fall back, if unlock does not work.
     */
    private static final String FALLBACK_PIN = "0000";

    private OnUARTManagerStatusChange listener;

    private UARTCallbackHandler callbackHandler;

    /**
     * FIFO double-ended queue holding all commands to send via the UART interface.
     * It is a dequeue, because we need to be able to add a command dynamically in front of the
     * succeeding ones.
     */
    private Deque<UARTCommand> commandQueue = new LinkedBlockingDeque<UARTCommand>();

    private int totalCommandAmount = 0;

    private boolean afterLockCheck;

    private UARTCommandList successfulCommands;

    private BluetoothGatt gatt;
    private BluetoothCoordinator bluetoothCoordinator;
    private urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice currentDevice;

    private static final int COMMAND_TRIES = 3;

    /**
     * Whether the latest command has exceeded its
     * retry count or not.
     */
    private boolean commandTriesExceeded = false;

    private boolean stopped = false;
    private boolean unlockDevice = true;
    private boolean unlockFallbackUsed = false;

    public UARTManager(Activity context, ApplicationProperties props, BluetoothCoordinator bluetoothCoordinator, OnUARTManagerStatusChange listener) {
        super(context, props);
        this.listener = listener;
        this.bluetoothCoordinator = bluetoothCoordinator;
    }

    @Override
    public void onGattCharacteristicChange(BluetoothGattCharacteristic characteristic) {

        Log.d(LOGGING_TAG, "Reading data from device '" + this.currentDevice + "'...");

        try {
            this.getCurrentCommand().addResponse(this.getCurrentDevice(), characteristic, this.successfulCommands);
        } catch (Throwable t) {
            Log.e(LOGGING_TAG, "Exception while adding response to command '" + this.getCurrentCommand() + "': " + t.getMessage());
            this.listener.onDeviceDisconnected(false);
        }

    }

    @Override
    public void onGattCharacteristicWriteFailed(Throwable t, BluetoothGattDescriptor descriptor, int status) {
        Log.e(LOGGING_TAG, t.getMessage());
        // TODO check
    }

    @Override
    public void onGattStatusChanged(int status, int newState) {
        if (newState == BluetoothGatt.STATE_CONNECTING) {
            this.listener.onDeviceConnecting();
        } else if (newState == BluetoothGatt.STATE_CONNECTING) {
            this.listener.onDeviceConnected();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            this.onBluetoothDeviceDisconnected();
        }
    }

    private void onBluetoothDeviceDisconnected() {

        this.gatt.close();

        if (this.getCurrentCommand() == null) {
            Log.d(LOGGING_TAG, "onBluetoothDeviceDisconnected() - Command is null");
            return;
        }

        if (this.getCurrentCommand().getType() == UARTCommandType.LOCK_CHECK_COMMAND && this.getCurrentCommand().getResponses().size() > 0) {

            boolean isUnlocked = (boolean) this.getCurrentCommand().getResponses().get(0).getValue();
            this.finalizeCommand();

            if (this.unlockDevice && !isUnlocked && !this.unlockFallbackUsed && this.afterLockCheck) { // unlock failed, retry with fallback

                this.unlockFallbackUsed = true;
                String logMsg = "Device is still locked, falling back to PIN " + FALLBACK_PIN + " unlock.";
                Log.d(LOGGING_TAG, logMsg);
                BeaconLogger.warn(this.currentDevice, logMsg);
                this.commandQueue.addFirst(UARTCommandType.LOCK_CHECK_COMMAND.getCommand());
                this.commandQueue.addFirst(UARTCommandType.LOCK_UNLOCK_COMMAND.getCommand(FALLBACK_PIN));

            } else if (this.afterLockCheck) { // check if (un-)lock was successful

                this.afterLockCheck = false;
                if (isUnlocked ^ unlockDevice) {
                    // failed (un-)lock
                    BeaconLogger.error(this.currentDevice, "Could not " + (unlockDevice ? "unlock" : "lock") + " device.");
                    BeaconLogger.status(this.getCurrentDevice(), BeaconStatus.LOCKED, this.props);
                    this.listener.onDeviceExecutionFailed(this.getCurrentDevice());
                    return;
                } else {
                    // successful (un-)lock
                    this.unlockDevice = !this.unlockDevice;
                    BeaconLogger.info(this.currentDevice, "Device was successfully " + (isUnlocked ? "unlocked" : "locked") + ".");
                }

            } else if (isUnlocked ^ unlockDevice) { // check if we need to (un-)lock

                String logMsg = "Device seems to be " + (isUnlocked ? "unlocked" : "locked")
                        + ", " + (unlockDevice ? "unlocking" : "locking") + " now.";
                Log.d(LOGGING_TAG, logMsg);
                BeaconLogger.debug(this.currentDevice, logMsg);

                this.afterLockCheck = true;
                this.commandQueue.addFirst(
                        UARTCommandType.LOCK_CHECK_COMMAND.getCommand()
                );
                this.commandQueue.addFirst(
                        UARTCommandType.LOCK_UNLOCK_COMMAND.getCommand(String.valueOf(this.getCurrentDevice().getBeacon().getSettings().getPin()))
                );

            } else {

                // we don't need to (un-)lock
                this.unlockDevice = !this.unlockDevice;
                BeaconLogger.info(this.currentDevice, "Device was already unlocked.");

            }

            if (this.commandQueue.size() > 0) {
                this.connectAndExecuteCommand();
            } else {
                if (this.stopped) {
                    this.listener.onDeviceCancelled(this.getCurrentDevice());
                } else {
                    this.listener.onDeviceExecuted(this.getCurrentDevice());
                }
            }

        } else if (this.getCurrentCommand().isPotentiallyDone()) {

            Log.d(LOGGING_TAG, "Command '" + this.getCurrentCommand() + "' on device '" + this.currentDevice + "' execution done.");
            this.listener.onDeviceCommandExecutionEnd(
                    this.stopped,
                    this.totalCommandAmount,
                    this.totalCommandAmount - this.commandQueue.size(),
                    this.getCurrentCommand()
            );
            this.successfulCommands.add(this.finalizeCommand());
            if (this.commandQueue.size() == 0) {
                this.listener.onDeviceDisconnected(true);
                if (this.stopped) {
                    this.listener.onDeviceCancelled(this.getCurrentDevice());
                } else {
                    this.listener.onDeviceExecuted(this.getCurrentDevice());
                }
                if (this.commandTriesExceeded) {
                    this.commandTriesExceeded = false;
                }
            } else {
                this.connectAndExecuteCommand();
            }

        } else {

            Log.e(LOGGING_TAG, "BLE device was disconnected before command '" + this.getCurrentCommand() + "' could be executed.");
            BeaconLogger.warn(this.getCurrentDevice(), "Device disconnected before command '" + this.getCurrentCommand() + "' could be executed.");
            BeaconLogger.status(this.getCurrentDevice(), BeaconStatus.INVALID_SETTINGS, this.props);

            int tries = this.getCurrentCommand().getTries();
            if (tries >= COMMAND_TRIES) {
                Log.e(LOGGING_TAG, "Command '" + this.getCurrentCommand() + "' could not be executed after " + tries + " tries. Cancelling communication with device and retrying BLE connection.");
                this.commandQueue.clear();
                this.lockDevice();
                this.commandTriesExceeded = true;
                this.listener.onDeviceExecutionFailed(this.getCurrentDevice());
                return;
            } else {
                this.listener.onDeviceDisconnected(false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                this.getCurrentCommand().nextTry();
            }

            this.connectAndExecuteCommand();

        }

    }

    private void lockDevice() {
        this.unlockDevice = false;
        this.commandQueue.addFirst(
                UARTCommandType.LOCK_CHECK_COMMAND.getCommand()
        );
    }

    public void start(BluetoothDevice device) {

        this.afterLockCheck = false;
        this.commandTriesExceeded = false;
        this.unlockDevice = true;
        this.stopped = false;
        this.currentDevice = device;
        this.callbackHandler = new UARTCallbackHandler(this, this);
        this.callbackHandler.setDevice(new UARTDevice(this.getCurrentDevice()));
        this.commandQueue.clear();
        this.successfulCommands = new UARTCommandList();

        this.populateCommands();
        this.connectAndExecuteCommand();

    }

    public void stop(boolean gracefulStop) {

        Log.i(LOGGING_TAG, "Stopping UARTManager (graceful? " + gracefulStop + ")...");
        this.stopped = true;
        this.callbackHandler.disable();
        this.callbackHandler = new UARTCallbackHandler(this, this);
        this.callbackHandler.setDevice(new UARTDevice(this.getCurrentDevice()));
        this.listener.onDeviceCancel(this.getCurrentDevice());

        if (gracefulStop) {
            this.commandQueue.clear();
            this.lockDevice();
            this.connectAndExecuteCommand();
        } else {
            this.commandQueue.clear();
            this.gatt.close();
            this.currentDevice = null;
            this.listener.onDeviceCancelled(null);
        }

        Log.i(LOGGING_TAG, "UARTManager successfully stoppped.");

    }

    private void onBluetoothError(Throwable t) {
        Log.e(LOGGING_TAG, "BLE device was disconnected before command, exception when finding devices: " + t.getMessage());
        this.listener.onDeviceDisconnected(false);
    }

    public void populateCommands() {

        this.commandQueue.offer(UARTCommandType.LOCK_CHECK_COMMAND.getCommand());
        this.commandQueue.offer(UARTCommandType.TELEMETRICS_COMMAND.getCommand());
        this.commandQueue.offer(UARTCommandType.SETTINGS_COMMAND.getCommand());
        this.commandQueue.offer(UARTCommandType.LOGGER_COMMAND.getCommand());
        this.commandQueue.offer(UARTCommandType.LOCK_CHECK_COMMAND.getCommand());

        this.totalCommandAmount = this.commandQueue.size();

    }

    public void connectAndExecuteCommand() {

        Log.d(LOGGING_TAG, "Starting execution of command '" + this.getCurrentCommand() + "' on device '" + this.currentDevice + "'...");

        this.listener.onDeviceCommandExecutionStart(
                this.stopped,
                this.totalCommandAmount,
                this.totalCommandAmount - this.commandQueue.size(),
                this.getCurrentCommand()
        );

        this.gatt = this.getCurrentDevice().getNativeDevice().connectGatt(this.context, false, this.callbackHandler);
        this.gatt.connect();

    }

    public UARTCommand finalizeCommand() {
        return this.commandQueue.poll();
    }

    public UARTCommand getCurrentCommand() {
        return this.commandQueue.peek();
    }

    public urbantrees.spaklingscience.at.urbantrees.bluetooth.BluetoothDevice getCurrentDevice() {
        return currentDevice;
    }

    public UARTCommandList getSuccessfulCommands() {
        return successfulCommands;
    }

    public interface OnUARTManagerStatusChange {
        void onDeviceConnecting();
        void onDeviceConnected();
        void onDeviceDisconnected(boolean isSuccessful);
        void onDeviceExecuted(BluetoothDevice device);
        void onDeviceCancel(BluetoothDevice device);
        void onDeviceCancelled(BluetoothDevice device);
        void onDeviceExecutionFailed(BluetoothDevice device);
        void onDeviceCommandExecutionStart(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command);
        void onDeviceCommandExecutionEnd(boolean cancelled, int totalCommandAmount, int currentCommandPosition, UARTCommand command);
    }

}
