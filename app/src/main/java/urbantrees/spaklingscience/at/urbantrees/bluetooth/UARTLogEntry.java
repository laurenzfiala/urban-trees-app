package urbantrees.spaklingscience.at.urbantrees.bluetooth;

import java.util.Date;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/20
 */
public class UARTLogEntry {

    private Date observationDate; // TODO remove

    private double temperature;
    private double humidity;
    private double dewPoint;

    public UARTLogEntry(Date observationDate, double temperature, double humidity, double dewPoint) {
        this.observationDate = observationDate;
        this.temperature = temperature;
        this.humidity = humidity;
        this.dewPoint = dewPoint;
    }

    public Date getObservationDate() {
        return observationDate;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public void setObservationDate(Date observationDate) {
        this.observationDate = observationDate;
    }
}
