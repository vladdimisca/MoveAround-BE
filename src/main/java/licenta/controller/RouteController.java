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
    @Path("/user/{userId}")
    public Response getRoutesByUserId(@PathParam("userId") UUID userId) throws UserNotFoundException {
        List<Route> routes = routeService.getRoutesByUserId(userId);
        return Response.ok(routes.stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }

    @POST
    @Path("/matching")
    @Authenticated
    public Response getPossibleRoutes(Route route) throws FailedToParseTheBodyException {
        List<Route> routes = routeService.getPossibleRoutes(route);
        return Response.ok(routes.stream().map(RouteMapper.mapper::fromRoute).collect(Collectors.toList())).build();
    }
}
