package licenta.model;

import org.checkerframework.common.value.qual.MinLen;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotBlank(message = "You have to provide the license plate")
    @Column(name="license_plate", nullable = false, unique = true)
    private String licensePlate;

    @NotBlank(message = "You have to provide the make")
    @Column(nullable = false)
    private String make;

    @NotBlank(message = "You have to provide the model")
    @Column(nullable = false)
    private String model;

    @NotBlank(message = "You have to provide the color")
    @Column(nullable = false)
    private String color;

    @Min(message = "A intrat aici", value = 2000)
    @NotBlank(message = "You have to provide the year")
    @Column(nullable = false)
    private Integer year;

    @ManyToOne(optional = false)
    @JsonbTransient
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
