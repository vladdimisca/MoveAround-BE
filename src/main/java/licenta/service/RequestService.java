package licenta.service;

import licenta.dao.RequestDAO;
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

    @Transactional
    public Request createRequest(Request request) throws FailedToParseTheBodyException,
            RouteNotFoundException, UserNotFoundException, ForbiddenActionException {

        routeValidator.validateCoordinates(request.getStartLatitude(), request.getStopLongitude(),
                request.getStopLatitude(), request.getStopLongitude());

        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        Route route = routeService.getRouteById(request.getRoute().getId());
        if (route.getUser().getId().equals(userId)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.CONFLICT, "Cannot create a request to your own route");
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

    public void acceptRequest(Integer requestId) throws RequestNotFoundException {
        Request request = getRequestById(requestId);
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

}
