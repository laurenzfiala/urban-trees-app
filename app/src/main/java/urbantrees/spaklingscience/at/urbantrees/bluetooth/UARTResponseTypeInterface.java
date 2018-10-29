package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * TODO
 * @since 2018/05/19
 * @param <T>
 */
interface UARTResponseTypeInterface<T> {

    /**
     * TODO
     * @param pkg
     * @return
     * @throws Throwable
     */
    UARTResponse<T> getResponse(final UARTResponsePackage pkg) throws Throwable;

    /**
     * Called every time the command gets executed. It may then calculate
     * the amount of necessary responses that must be attached to this response type.
     *
     * - 0      ... no restriction on the response amount
     * - 1..n   ... n responses fot this type must be received
     *
     * @param pkg TODO
     * @return
     * @throws Throwable
     */
    int getResponseAmount(final UARTResponsePackage pkg) throws Throwable;

}
