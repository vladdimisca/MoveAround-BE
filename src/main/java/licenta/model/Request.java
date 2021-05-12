package licenta.model;

import licenta.util.enumeration.Status;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "start_latitude", nullable = false)
    private double startLatitude;

    @Column(name = "start_longitude", nullable = false)
    private double startLongitude;

    @Column(name = "stop_latitude", nullable = false)
    private double stopLatitude;

    @Column(name = "stop_longitude", nullable = false)
    private double stopLongitude;

    private Status status;

    @ManyToOne(optional = false)
    @JsonbTransient
    private User user;

    @ManyToOne(optional = false)
    private Route route;

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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
