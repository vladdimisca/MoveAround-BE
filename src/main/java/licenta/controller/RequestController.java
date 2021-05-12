package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.exception.definition.ForbiddenActionException;
import licenta.exception.definition.RouteNotFoundException;
import licenta.exception.definition.UserNotFoundException;
import licenta.mapper.RequestMapper;
import licenta.model.Request;
import licenta.service.RequestService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RequestController {

    @Inject
    RequestService requestService;

    @POST
    @Authenticated
    public Response createRequest(Request request) throws UserNotFoundException,
            FailedToParseTheBodyException, RouteNotFoundException, ForbiddenActionException {

        return Response.ok(RequestMapper.mapper.fromRequest(requestService.createRequest(request))).build();
    }

    @GET
    @Path("/sent")
    public Response getSentRequests() {
        List<Request> sentRequests = requestService.getSentRequests();
        return Response.ok(
                sentRequests.stream().map(RequestMapper.mapper::fromRequest).collect(Collectors.toList())).build();
    }

    @GET
    @Path("received")
    public Response getReceivedPendingRequests() throws UserNotFoundException {
        List<Request> receivedPending = requestService.getReceivedPendingRequests();
        return Response.ok(
                receivedPending.stream().map(RequestMapper.mapper::fromRequest).collect(Collectors.toList())).build();
    }

}
