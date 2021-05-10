package urbantrees.spaklingscience.at.urbantrees.entities;

/**
 * Holds all beacon transfer states.
 * Used to communicate with the webapp for display of states.
 * 
 * @author Laurenz Fiala
 * @since 2021/05/09
 */
public enum TransferStatus {

	NOT_YET_STARTED(true),
	PREPARE_READOUT(false),
	COMM_DEVICE_GET_SETTINGS(false),
	COMM_DEVICE_CONNECTING(false),
	COMM_DEVICE_CONNECTED(false),
	COMM_DEVICE_GET_DATA(false),
	COMM_DEVICE_GET_DATA_FAILED(true),
	COMM_DEVICE_SEND_DATA(false),
	COMM_DEVICE_SEND_DATA_FAILED(true),
	COMM_DEVICE_READOUT_FINISHED(true),
	COMM_DEVICE_CANCELLING(false),
	COMM_DEVICE_CANCELLED(true);

	private final boolean showSearchControls;

	private TransferStatus(final boolean showSearchControls) {
		this.showSearchControls = showSearchControls;
	}
	public boolean isShowSearchControls() {
		return this.showSearchControls;
	}
	
}
