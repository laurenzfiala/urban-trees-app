package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * Rules for receiving {@link UARTResponse}s from the device
 * and how the {@link UARTResponseType}s may be reused or not.
 * See documentation of enum constants for more information on their ruleset.
 *
 * @author Laurenz Fiala
 * @since 2018/05/19
 */
enum UARTResponseStrategy {

    /**
     * - skip unmatched responses
     * - excess responses (after last applied type) cause an error
     */
    SKIP_UNMATCHED,

    /**
     * - skip unmatched responses
     * - reuse last type for all outstanding responses
     */
    SKIP_UNMATCHED_REUSE_LAST,

    /**
     * - types are strictly applied in order
     * - if a type does not match the result, error
     * - excess responses (more than given types) cause an error
     */
    ORDERED_STRICT,

    /**
     * - types are strictly applied in order
     * - if a type does not match the result, error
     * - reuse last type for all outstanding responses
     */
    ORDERED_STRICT_REUSE_LAST

}
