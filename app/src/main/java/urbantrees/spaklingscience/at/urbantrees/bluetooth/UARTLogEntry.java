package urbantrees.spaklingscience.at.urbantrees.bluetooth;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2018/05/20
 */
public class UARTLogEntry {

    private long observationDate;

    private double temperature;
    private double humidity;
    private double dewPoint;

    public UARTLogEntry(long observationDate, double temperature, double humidity, double dewPoint) {
        this.observationDate = observationDate;
        this.temperature = temperature;
        this.humidity = humidity;
        this.dewPoint = dewPoint;
    }

    public long getObservationDate() {
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

}
