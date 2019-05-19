package urbantrees.spaklingscience.at.urbantrees.entities;

import java.util.Date;

/**
 * A single log entry to be sent to the backend.
 * @author Laurenz Fiala
 * @since 2019/05/19
 */
public class BeaconLog {

    /**
     * The logs' database identifier.
     */
    private int id;

    /**
     * ID of associated {@link Beacon}.
     */
    private int beaconId;

    /**
     * Log severity.
     */
    private BeaconLogSeverity severity;

    /**
     * Brief message type.
     */
    private BeaconLogType type;

    /**
     * More precise message describing the event.
     */
    private String message;

    /**
     * Date of the event happening.
     */
    private Date eventDate;

    /**
     * Optional ID of the settings entry active
     * at the time of logging this event.
     */
    private Integer settingsId;

    /**
     * Default Constructor.
     */
    public BeaconLog() {
    }

    /**
     * Constructor for manual log messages inserts.
     */
    public BeaconLog(
            int beaconId,
            BeaconLogSeverity severity,
            BeaconLogType type,
            String message,
            Date eventDate) {
        this(0, beaconId, severity, type, message, eventDate, null);
    }

    /**
     * Default constructor for all members.
     */
    public BeaconLog(
            int id,
            int beaconId,
            BeaconLogSeverity severity,
            BeaconLogType type,
            String message,
            Date eventDate,
            Integer settingsId) {
        this.id = id;
        this.beaconId = beaconId;
        this.severity = severity;
        this.type = type;
        this.message = message;
        this.eventDate = eventDate;
        this.settingsId = settingsId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(int beaconId) {
        this.beaconId = beaconId;
    }

    public BeaconLogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(BeaconLogSeverity severity) {
        this.severity = severity;
    }

    public BeaconLogType getType() {
        return type;
    }

    public void setType(BeaconLogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(Integer settingsId) {
        this.settingsId = settingsId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

}
