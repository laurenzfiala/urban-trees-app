package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author Laurenz Fiala
 * @since 2018/05/14
 */
public class UARTCommand {

    private static final String LOGGING_TAG = UARTCommand.class.getName();

    private UARTCommandType type;

    /**
     * Encoding currently used for all UART communication.
     */
    public static final Charset ENCODING = Charset.forName("US-ASCII");

    /**
     * Command to be written to the receiving
     * UART characteristic of the device.
     */
    private final String inputCommand;

    /**
     * Strategy to use when a new response is received.
     * They define certain rules for the {@link UARTResponseType}s
     * defined in the command instance and how they may be reused or not.
     */
    private final UARTResponseStrategy responseStrategy;

    private final UARTResponseType[] responseTypes;

    private int tries = 0;

    /**
     * TODO
     */
    private List<byte[]> collatedCharacteristics = new ArrayList<byte[]>();;

    private int responseTypeIndex = 0;

    /**
     * TODO
     */
    private int totalResponseAmount = 0;

    private List<UARTResponse> responses = new ArrayList<UARTResponse>();

    public UARTCommand(final UARTCommandType type, final String inputCommand, final UARTResponseStrategy responseStrategy, final UARTResponseType... responseTypes) {
        this.type = type;
        this.inputCommand = inputCommand;
        this.responseStrategy = responseStrategy;
        this.responseTypes = responseTypes;
    }

    public byte[] getCommandBytes() {
        return this.inputCommand.getBytes(ENCODING);
    }

    /**
     * Add a response to this command execution.
     * Depending on the {@link UARTResponseStrategy}, this may return
     * a boolean value indicating successful adherence to the strategy's policies.
     * @param device bluetooth device from which the response was received
     * @param characteristic received from the device after command was written to it
     * @param cmds all previous commands
     * @return true if the given response adheres to the set {@link UARTResponseStrategy}; false if not
     * @throws Throwable if the extraction of the needed information failed unexpectedly
     */
    public boolean addResponse(final BluetoothDevice device,
                               final BluetoothGattCharacteristic characteristic,
                               final UARTCommandList cmds) throws Throwable {

        this.totalResponseAmount++;
        if (this.responseTypes.length <= this.responseTypeIndex) {

            switch (this.responseStrategy) {
                case SKIP_UNMATCHED:
                    return true;
                case SKIP_UNMATCHED_REUSE_LAST:
                case ORDERED_STRICT_REUSE_LAST:
                    this.responseTypeIndex--; // use last if not enough types
                    break;
                default:
                    throw new RuntimeException("Amount of UART response types exceeded, but strategy is " + this.responseStrategy);
            }

        }

        this.collatedCharacteristics.add(characteristic.getValue());
        final UARTResponsePackage pkg =  new UARTResponsePackage(
                this.totalResponseAmount,
                this.responses.size(),
                this.collatedCharacteristics.toArray(new byte[this.collatedCharacteristics.size()][]),
                cmds,
                device
        );
        final int responseAmount = this.getCurrentResponseType().getResponseAmount(pkg);

        if (responseAmount == 0) {
            this.collatedCharacteristics = new ArrayList<byte[]>();
            this.responseTypeIndex++;
        } else if (responseAmount == -1 || responseAmount > this.collatedCharacteristics.size()) {
            return true;
        } else if (this.getCurrentResponseType().getResponseAmount(pkg) < this.collatedCharacteristics.size()) { // TODO only if skip strategy
            this.collatedCharacteristics.remove(0);
            pkg.setCharacteristics(this.collatedCharacteristics.toArray(new byte[this.collatedCharacteristics.size()][]));
        }

        final UARTResponse response = this.getCurrentResponseType().getResponse(pkg);
        if (response == null) {

            switch (this.responseStrategy) {
                case SKIP_UNMATCHED:
                case SKIP_UNMATCHED_REUSE_LAST:
                    return true;
                default:
                    Log.e(LOGGING_TAG, "UART response was empty, but strategy is " + this.responseStrategy);
                    return false;
            }

        } else {
            this.collatedCharacteristics = new ArrayList<byte[]>();
            this.responses.add(response);
            this.responseTypeIndex++;
        }

        return true;

    }

    public UARTResponseType getCurrentResponseType() throws RuntimeException {
        return this.responseTypes[this.responseTypeIndex];
    }

    public List<UARTResponse> getResponses() {
        return this.responses;
    }

    /**
     * Find the given {@link UARTResponse} by its type.
     * If the given type is not found, return null.
     * @param typeToFind {@link UARTResponseType} of the {@link UARTResponse} to find
     * @return found {@link UARTResponse} or null
     */
    public <T> UARTResponse<T> findResponse(UARTResponseType typeToFind) {
        for (UARTResponse r : this.getResponses()) {
            if (r.getType() == typeToFind) {
                return r;
            }
        }
        return null;
    }

    /**
     * TODO
     * @return
     */
    public boolean isPotentiallyDone() {
        return this.responseTypeIndex >= this.responseTypes.length;
    }

    /**
     * TODO
     * @return
     */
    public int getProgress() {
        return this.responseTypeIndex / this.responseTypes.length * 100;
    }

    @Override
    public String toString() {
        return this.inputCommand;
    }

    public int getTries() {
        return tries;
    }

    public void nextTry() {
        this.tries++;
    }

    public UARTCommandType getType() {
        return type;
    }
}
