package licenta.service;

import licenta.dao.RequestDAO;
import licenta.dao.RouteDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.*;
import licenta.model.Request;
import licenta.model.Route;
import licenta.util.enumeration.Authentication;
import licenta.util.enumeration.Status;
import licenta.validator.RouteValidator;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RequestService {

    @Inject
    RequestDAO requestDAO;

    @Inject
    RouteValidator routeValidator;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @Inject
    RouteService routeService;

    @Inject
    RouteDAO routeDAO;

    @Transactional
    public Request createRequest(Request request) throws FailedToParseTheBodyException,
            RouteNotFoundException, UserNotFoundException, ForbiddenActionException {

        routeValidator.validateCoordinates(request.getStartLatitude(), request.getStopLongitude(),
                request.getStopLatitude(), request.getStopLongitude());
        // TODO: check not to be a subroute
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        Route route = routeService.getRouteById(request.getRoute().getId());
        if (route.getUser().getId().equals(userId)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.CONFLICT, "Cannot create a request to your own route");
        }
        if (route.getAvailableSeats() <= route.getSubRoutes().size()) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "Seats capacity has been reached");
        }

        // check if the current user already made a request to this route and the request is pending or accepted
        boolean check = route.getRequests().stream()
                .filter(req -> !req.getStatus().equals(Status.REJECTED))
                .map(req -> req.getUser().getId())
                .anyMatch(userId::equals);

        if (check) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.CONFLICT, "You have already created a request to this route");
        }

        request.setUser(userService.getUserById(userId));
        request.setStatus(Status.PENDING);
        requestDAO.persist(request);

        return request;
    }

    public List<Request> getSentRequests() {
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        return requestDAO.getRequestsByUserId(userId);
    }

    public List<Request> getReceivedPendingRequests() throws UserNotFoundException {
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        return userService.getUserById(userId).getRoutes().stream()
                .map(Route::getRequests)
                .flatMap(Collection::stream)
                .filter(request -> request.getStatus().equals(Status.PENDING))
                .collect(Collectors.toList());
    }

    public Request getRequestById(Integer requestId) throws RequestNotFoundException {
        return requestDAO.getRequestById(requestId).orElseThrow(() ->
                new RequestNotFoundException(ExceptionMessage.REQUEST_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    @Transactional
    public void acceptRequest(Integer requestId) throws RequestNotFoundException, ForbiddenActionException {
        Request request = getRequestById(requestId);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (!request.getRoute().getUser().getId().equals(userId)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You are not allowed to accept this request");
        }
        if (!request.getStatus().equals(Status.PENDING)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You can accept only pending requests");
        }
        if (request.getRoute().getAvailableSeats() <= request.getRoute().getSubRoutes().size()) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "Seats capacity has been reached");
        }

        Route subRoute = new Route();
        subRoute.setStartLatitude(request.getStartLatitude());
        subRoute.setStartLongitude(request.getStartLongitude());
        subRoute.setStopLatitude(request.getStopLatitude());
        subRoute.setStopLongitude(request.getStopLongitude());
        subRoute.setStartDate(request.getRoute().getStartDate());
        subRoute.setParentRoute(request.getRoute());
        subRoute.setUser(request.getUser());
        subRoute.setPrice(24.0);// TODO: set the price

        request.setStatus(Status.ACCEPTED);
        requestDAO.persist(request);
        routeDAO.persist(subRoute);
    }

    @Transactional
    public void rejectRequest(Integer requestId) throws RequestNotFoundException, ForbiddenActionException {
        Request request = getRequestById(requestId);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (!request.getRoute().getUser().getId().equals(userId)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You are not allowed to reject this request");
        }
        if (!request.getStatus().equals(Status.PENDING)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You can reject only pending requests");
        }

        request.setStatus(Status.REJECTED);
        requestDAO.persist(request);
    }

    @Transactional
    public void removeRequest(Integer requestId) throws RequestNotFoundException, ForbiddenActionException {
        Request request = getRequestById(requestId);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (!request.getUser().getId().equals(userId)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "This request belongs to another user");
        }

        requestDAO.delete(request);
    }

    public static double computeDistance(double firstLatitude, double firstLongitude,
                                  double secondLatitude, double secondLongitude) {

        final int R = 6371; // radius of the earth

        double latDistance = Math.toRadians(secondLatitude - firstLatitude);
        double lonDistance = Math.toRadians(secondLongitude - firstLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(firstLatitude)) * Math.cos(Math.toRadians(secondLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

}
