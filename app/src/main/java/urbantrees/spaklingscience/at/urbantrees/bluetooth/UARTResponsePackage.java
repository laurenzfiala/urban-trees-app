package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * TODO
 *
 * @author Laurenz Fiala
 * @since 2018/05/21
 */
public class UARTResponsePackage {

    private int totalResponseAmount;

    private int matchedResponseAmount;

    private byte[][] characteristics;

    private UARTCommandList previousCommands;

    public UARTResponsePackage(
            final int totalResponseAmount,
            final int matchedResponseAmount,
            final byte[][] characteristics,
            final UARTCommandList previousCommands
    ) {
        this.totalResponseAmount = totalResponseAmount;
        this.matchedResponseAmount = matchedResponseAmount;
        this.characteristics = characteristics;
        this.previousCommands = previousCommands;
    }

    public int getTotalResponseAmount() {
        return totalResponseAmount;
    }

    public byte[] getCharacteristic(int position) {
        return this.characteristics[position];
    }

    public int getMatchedResponseAmount() {
        return matchedResponseAmount;
    }

    public byte[][] getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(byte[][] characteristics) {
        this.characteristics = characteristics;
    }

    public UARTCommandList getPreviousCommands() {
        return previousCommands;
    }

    public void setPreviousCommands(UARTCommandList previousCommands) {
        this.previousCommands = previousCommands;
    }
}
