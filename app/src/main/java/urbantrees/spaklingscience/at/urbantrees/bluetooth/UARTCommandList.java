package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import java.util.ArrayList;

/**
 * TODO
 *
 * @author Laurenz Fiala
 * @since 2018/05/21
 */
public class UARTCommandList extends ArrayList<UARTCommand> {

    /**
     * Find the first {@link UARTCommand} matching the given command-type
     * and return it.
     * @param type {@link UARTCommandType} to find
     * @return UARTCommand or null if no matching command was found
     */
    public UARTCommand find(UARTCommandType type) {
        for (UARTCommand c : this) {
            if (c.getType() == type) {
                return c;
            }
        }
        return null;
    }

    public <T> UARTResponse<T> findResponse(UARTResponseType type) {

        for (UARTCommand command : this) {
            for (UARTResponse response : command.getResponses()) {

                if (response.getType() == type) {
                    return (UARTResponse<T>) response;
                }

            }
        }

        return null;

    }

}
