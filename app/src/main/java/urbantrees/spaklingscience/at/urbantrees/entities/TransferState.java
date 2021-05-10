package urbantrees.spaklingscience.at.urbantrees.entities;

/**
 * Holds a beacon transfer state.
 * 
 * @author Laurenz Fiala
 * @since 2021/05/10
 */
public class TransferState {

	/**
	 * Status this state is associated with.
	 */
	private TransferStatus status;

	/**
	 * Overall progress in percent (0-100; -1 means indeterminate).
	 */
	private int progress;

	public TransferState(TransferStatus status) {
		this(status, -1);
	}

	public TransferState(TransferStatus status, int progress) {
		this.status = status;
		this.progress = progress;
	}

	/**
	 * Update this state with the given one. The given state must be of the same {@link #status}
	 * and may not be null.
	 * @param state state to update this state with
	 * @return this
	 */
	public TransferState updateWith(TransferState state) {
		if (this == state) {
			return this;
		}
		if (this.getStatus() != state.getStatus()) {
			throw new RuntimeException("Updates to a status may only be done with states that have the same status.");
		}
		this.progress = state.progress;
		return this;
	}

	public TransferStatus getStatus() {
		return status;
	}

	public int getProgress() {
		return progress;
	}

}
