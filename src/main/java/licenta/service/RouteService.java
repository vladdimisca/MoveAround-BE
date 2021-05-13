package licenta.service;

import io.quarkus.security.Authenticated;
import licenta.dao.RouteDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.*;
import licenta.model.Route;
import licenta.util.enumeration.Authentication;
import licenta.validator.RouteValidator;
import licenta.validator.ValidationMode;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RouteService {

    @Inject
    RouteDAO routeDAO;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @Inject
    RouteValidator routeValidator;

    @Inject
    CarService carService;

    @Transactional
    @Authenticated
    public Route createRoute(Route route) throws UserNotFoundException, CarNotFoundException,
            FailedToParseTheBodyException, ForbiddenActionException {

        routeValidator.validate(route, ValidationMode.CREATE);

        route.setId(0);
        route.setCar(carService.getCarById(route.getCar().getId()));

        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        route.setUser(userService.getUserById(userId));

        if (!route.getUser().getCars().contains(route.getCar())) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "Selected car does not belong to this user");
        }

        routeDAO.persist(route);
        return route;
    }

    public Route getRouteById(Integer routeId) throws RouteNotFoundException {
        return routeDAO.getRouteById(routeId).orElseThrow(() ->
                new RouteNotFoundException(ExceptionMessage.ROUTE_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public List<Route> getRoutesAsDriver(UUID userId) throws UserNotFoundException {
        userService.checkUserExistenceById(userId);
        List<Route> routes = routeDAO.getRoutesByUserId(userId);
        return routes.stream().filter(route -> route.getParentRoute() == null).collect(Collectors.toList());
    }

    public List<Route> getRoutesAsPassenger(UUID userId) throws UserNotFoundException {
        userService.checkUserExistenceById(userId);
        List<Route> routes = routeDAO.getRoutesByUserId(userId);
        return routes.stream().filter(route -> route.getParentRoute() != null).collect(Collectors.toList());
    }

    public List<Route> getPossibleRoutes(Route route) throws FailedToParseTheBodyException {
        routeValidator.validateStartDate(route.getStartDate());
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        return routeDAO.getPossibleRoutes(userId, route.getStartDate());
    }
}
