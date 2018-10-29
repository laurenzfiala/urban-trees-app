package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * TODO
 * T = value type
 * @param <T>
 * @author Laurenz Fiala
 * @since 2018/05/17
 */
public class UARTResponse<T> {

    private UARTResponseType type;
    private T value;

    public UARTResponse(final UARTResponseType type, final T value) {
        this.type = type;
        this.value = value;
    }

    public UARTResponseType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

}
