package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.*;
import licenta.mapper.RouteMapper;
import licenta.model.Route;
import licenta.service.RouteService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class RouteController {

    @Inject
    RouteService routeService;

    @POST
    @Authenticated
    public Response createRoute(Route route) throws UserNotFoundException, CarNotFoundException,
            FailedToParseTheBodyException, ForbiddenActionException {

        return Response.ok(RouteMapper.mapper.fromRoute(routeService.createRoute(route))).build();
    }

    @GET
    @Authenticated
    @Path("/{routeId}")
    public Response getRouteById(@PathParam("routeId") Integer routeId) throws RouteNotFoundException {
        return Response.ok(RouteMapper.mapper.fromRoute(routeService.getRouteById(routeId))).build();
    }

    @GET
    @Authenticated
    @Path("/user/{userId}/driver")
    public Response getRoutesAsDriver(@PathParam("userId") UUID userId) throws UserNotFoundException {
        List<Route> routes = routeService.getRoutesAsDriver(userId);
        return Response.ok(routes.stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }

    @GET
    @Authenticated
    @Path("/user/{userId}/passenger")
    public Response getRoutesAsPassenger(@PathParam("userId") UUID userId) throws UserNotFoundException {
        List<Route> routes = routeService.getRoutesAsPassenger(userId);
        return Response.ok(routes.stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }

    @DELETE
    @Authenticated
    @Path("/{routeId}")
    public Response deleteRoute(@PathParam("routeId") Integer routeId)
            throws RouteNotFoundException, ForbiddenActionException {

        routeService.deleteRouteById(routeId);
        return Response.noContent().build();
    }

    @GET
    @Authenticated
    @Path("/{routeId}/waypoints")
    public Response getWaypoints(@PathParam("routeId") Integer routeId) throws RouteNotFoundException {
        Route route = routeService.getRouteById(routeId);
        return Response.ok(
                route.getSubRoutes().stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/matching/{startDate}")
    @Authenticated
    public Response getPossibleRoutes(@PathParam("startDate") LocalDateTime startDate)
            throws FailedToParseTheBodyException {

        List<Route> routes = routeService.getPossibleRoutes(startDate);
        return Response.ok(routes.stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }

}
