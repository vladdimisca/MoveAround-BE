package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.Car;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CarDAO implements PanacheRepository<Car> {

    public Optional<Car> getCarById(Integer carId) {
        return find("id", carId).firstResultOptional();
    }

    public List<Car> getCarsByUserId(UUID userId) {
        return find("user_id = ?1", userId).stream().collect(Collectors.toList());
    }

    public Optional<Car> getCarByLicensePlate(String licensePlate) {
        return find("license_plate = ?1", licensePlate).firstResultOptional();
    }
}
