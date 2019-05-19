package urbantrees.spaklingscience.at.urbantrees.entities;

/**
 * Summarized status of a beacon.
 * This is supposed to help quickly see issues with the beacon.
 * 
 * @author Laurenz Fiala
 * @since 2018/06/16
 */
public enum BeaconStatus {

	/**
	 * The beacon was just set up and there has been no actual
	 * access via the app and therefore no settings stored.
	 */
	INITIAL,
	
	/**
	 * The beacon is operational and connects & logs normally.
	 */
	OK,
	
	/**
	 * The beacon has an unknown PIN & needs to be reset.
	 */
	LOCKED,
	
	/**
	 * The beacon has invalid settings which prevent correct
	 * read-out of data or logs.
	 */
	INVALID_SETTINGS,
	
	/**
	 * The beacon has been marked deleted, but we don't actually want to delete anything.
	 */
	DELETED
	
}
