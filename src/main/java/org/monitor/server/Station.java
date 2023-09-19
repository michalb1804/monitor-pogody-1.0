package org.monitor.server;

import java.time.LocalDateTime;

public class Station {
    private String city;
    private LocalDateTime measurementTime;
    private float temperature;
    private int windSpeed;
    private float humidity;
    private float totalRainfall;
    private float pressure;

    public Station(String city, LocalDateTime measurementTime, float temperature, int windSpeed, float humidity, float totalRainfall, float pressure) {
        this.city = city;
        this.measurementTime = measurementTime;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.totalRainfall = totalRainfall;
        this.pressure = pressure;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(LocalDateTime measurementTime) {
        this.measurementTime = measurementTime;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTotalRainfall() {
        return totalRainfall;
    }

    public void setTotalRainfall(float totalRainfall) {
        this.totalRainfall = totalRainfall;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }
}
