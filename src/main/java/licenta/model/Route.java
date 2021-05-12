package licenta.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "start_date", nullable = false)
    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm")
    private LocalDateTime startDate;

    @Column(name = "start_latitude",nullable = false)
    private double startLatitude;

    @Column(name = "start_longitude",nullable = false)
    private double startLongitude;

    @Column(name = "stop_latitude",nullable = false)
    private double stopLatitude;

    @Column(name = "stop_longitude",nullable = false)
    private double stopLongitude;

    @Column(name="available_seats", nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private Double price;

    @ManyToOne(optional = false)
    private Car car;

    @ManyToOne(optional = false)
    @JsonbTransient
    private User user;

    @OneToMany(mappedBy = "route")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Request> requests;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
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

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
}
