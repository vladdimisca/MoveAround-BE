package licenta.dto;

import licenta.util.enumeration.Status;

import java.util.UUID;

public class RequestDTO {
    private int id;
    private double startLatitude;
    private double startLongitude;
    private double stopLatitude;
    private double stopLongitude;
    private Status status;
    private UUID userId;
    private Integer routeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getStopLatitude() {
        return stopLatitude;
    }

    public void setStopLatitude(double stopLatitude) {
        this.stopLatitude = stopLatitude;
    }

    public double getStopLongitude() {
        return stopLongitude;
    }

    public void setStopLongitude(double stopLongitude) {
        this.stopLongitude = stopLongitude;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }
}
