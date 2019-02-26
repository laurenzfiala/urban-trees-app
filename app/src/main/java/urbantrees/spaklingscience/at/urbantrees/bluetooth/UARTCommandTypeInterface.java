package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * Holds all command
 * @author Laurenz Fiala
 * @since 2019/02/24
 */
public interface UARTCommandTypeInterface {

    UARTCommand getCommand(String ...args);

}
