package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * Statuses emitted by the {@link UARTManager} to the caller.
 * These are asynchronously reported through
 * {@link urbantrees.spaklingscience.at.urbantrees.util.PropertyChangeEmitter}.
 *
 * @since 2018/05/19
 * @author Laurenz Fiala
 */
public enum UARTManagerStatus {

    /**
     * Emitted when starting a device search.
     */
    DEVICE_SEARCHING,

    /**
     * Emitted upon successful discovery of
     * a BLE device matching filters.
     */
    DEVICE_FOUND,

    /**
     * Emitted upon creating GATT connection
     * and subscribing to UART characteristics.
     */
    DEVICE_CONNECTING,

    /**
     * Emitted if the connection to the
     * found BLE/UART device failed.
     * This includes errors while accessing the UART
     * services and setting UART characteristic
     * notifications.
     */
    DEVICE_CONNECTION_FAILED,

    /**
     * Emitted when starting to fetch the devices'
     * information via the UART interface.
     */
    DEVICE_INFO_FETCH,

    /**
     * Emitted every time a data packet
     * is received.
     */
    DEVICE_INFO_PART_FETCHED,

    /**
     * Emitted when fetching of the devices'
     * information via the UART interface is done.
     */
    DEVICE_INFO_FETCHED,

    /**
     * Emitted if the transmission or
     * interpretation of commands from the device
     * can not finish successfully.
     */
    DEVICE_INFO_FETCH_FAILED,

    /**
     * Emitted when starting to upload the
     * fetched device info to the backend.
     */
    DEVICE_INFO_UPLOADING,

    /**
     * Emitted if the device info upload
     * failed.
     */
    DEVICE_INFO_UPLOAD_FAILED,

    /**
     * Emitted upon successful completion of
     * all routines associated with one remote
     * device.
     */
    DEVICE_INFO_UPLOADED

}
