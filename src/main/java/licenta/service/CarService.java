package licenta.service;

import licenta.dao.CarDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.*;
import licenta.model.Car;
import licenta.util.enumeration.Authentication;
import licenta.validator.CarValidator;
import licenta.validator.ValidationMode;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CarService {

    @Inject
    CarDAO carDAO;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @Inject
    CarValidator carValidator;

    @Transactional
    public Car createCar(Car car) throws UserNotFoundException, LicensePlateAlreadyExistsException,
            FailedToParseTheBodyException, ForbiddenActionException {

        carValidator.validate(car, ValidationMode.CREATE);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (getCarsByUserId(userId).size() >= 5) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You can not have more than 5 cars");
        }
        if (carDAO.getCarByLicensePlate(car.getLicensePlate()).isPresent()) {
            throw new LicensePlateAlreadyExistsException(
                    ExceptionMessage.LICENSE_PLATE_ALREADY_EXISTS, Response.Status.CONFLICT);
        }

        car.setId(0);
        car.setUser(userService.getUserById(userId));
        carDAO.persist(car);
        return car;
    }

    @Transactional
    public Car updateCarById(Integer carId, Car car) throws CarNotFoundException, ForbiddenActionException,
            LicensePlateAlreadyExistsException, FailedToParseTheBodyException {

        carValidator.validate(car, ValidationMode.UPDATE);

        Car existingCar = getCarById(carId);
        userService.checkIfUserIdMatchesToken(existingCar.getUser().getId());

        if (!existingCar.getLicensePlate().equals(car.getLicensePlate())
                && carDAO.getCarByLicensePlate(car.getLicensePlate()).isPresent()) {
            throw new LicensePlateAlreadyExistsException(
                    ExceptionMessage.LICENSE_PLATE_ALREADY_EXISTS, Response.Status.CONFLICT);
        }

        existingCar.setColor(car.getColor());
        existingCar.setLicensePlate(car.getLicensePlate());
        existingCar.setMake(car.getMake());
        existingCar.setModel(car.getModel());
        existingCar.setYear(car.getYear());
        carDAO.persist(existingCar);

        return existingCar;
    }

    public Car getCarById(Integer carId) throws CarNotFoundException {
        return carDAO.getCarById(carId).orElseThrow(() ->
                new CarNotFoundException(ExceptionMessage.CAR_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public List<Car> getCarsByUserId(UUID userId) throws UserNotFoundException {
        userService.checkUserExistenceById(userId);
        return carDAO.getCarsByUserId(userId);
    }

    @Transactional
    public void deleteCarById(Integer carId) throws CarNotFoundException, ForbiddenActionException {
        Car car = getCarById(carId);
        userService.checkIfUserIdMatchesToken(car.getUser().getId());
        carDAO.delete(car);
    }
}
