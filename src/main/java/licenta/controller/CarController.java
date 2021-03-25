package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.CarNotFoundException;
import licenta.exception.definition.ForbiddenActionException;
import licenta.exception.definition.LicensePlateAlreadyExistsException;
import licenta.exception.definition.UserNotFoundException;
import licenta.mapper.CarMapper;
import licenta.model.Car;
import licenta.service.CarService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/cars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CarController {

    @Inject
    CarService carService;

    @POST
    public Response createCar(Car car) throws UserNotFoundException, LicensePlateAlreadyExistsException {
        return Response.ok(CarMapper.mapper.fromCar(carService.createCar(car))).build();
    }

    @PUT
    @Path("/{carId}")
    public Response updateCarById(@PathParam("carId") Integer carId, Car car)
            throws CarNotFoundException, ForbiddenActionException, LicensePlateAlreadyExistsException {

        return Response.ok(CarMapper.mapper.fromCar(carService.updateCarById(carId, car))).build();
    }

    @GET
    @Path("/{carId}")
    public Response getCarById(@PathParam("carId") Integer carId) throws CarNotFoundException {
        return Response.ok(CarMapper.mapper.fromCar(carService.getCarById(carId))).build();
    }

    @GET
    @Authenticated
    @Path("/user/{userId}")
    public Response getAllCars(@PathParam("userId") UUID userId) throws UserNotFoundException {
        List<Car> cars = carService.getCarsByUserId(userId);
        return Response.ok(cars.stream().map(CarMapper.mapper::fromCar).collect(Collectors.toList())).build();
    }
}
