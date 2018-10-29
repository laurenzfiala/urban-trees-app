package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import java.util.ArrayList;

/**
 * TODO
 *
 * @author Laurenz Fiala
 * @since 2018/05/21
 */
public class UARTCommandList extends ArrayList<UARTCommand> {

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
