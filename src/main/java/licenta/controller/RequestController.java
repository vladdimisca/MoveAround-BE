package licenta.controller;

import licenta.exception.definition.*;
import licenta.mapper.RequestMapper;
import licenta.model.Request;
import licenta.service.RequestService;
import licenta.util.enumeration.Role;

import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed({ Role.Constants.USER })
    public Response createRequest(Request request) throws UserNotFoundException,
            FailedToParseTheBodyException, RouteNotFoundException, ForbiddenActionException {

        return Response.ok(RequestMapper.mapper.fromRequest(requestService.createRequest(request))).build();
    }

    @GET
    @Path("/sent")
    @RolesAllowed({ Role.Constants.USER })
    public Response getSentRequests() {
        List<Request> sentRequests = requestService.getSentRequests();
        return Response.ok(
                sentRequests.stream().map(RequestMapper.mapper::fromRequest).collect(Collectors.toList())).build();
    }

    @GET
    @Path("received")
    @RolesAllowed({ Role.Constants.USER })
    public Response getReceivedPendingRequests() throws UserNotFoundException {
        List<Request> receivedPending = requestService.getReceivedPendingRequests();
        return Response.ok(
                receivedPending.stream().map(RequestMapper.mapper::fromRequest).collect(Collectors.toList())).build();
    }

    @DELETE
    @Path("/{requestId}")
    @RolesAllowed({ Role.Constants.USER })
    public Response deleteRequest(@PathParam("requestId") Integer requestId)
            throws ForbiddenActionException, RequestNotFoundException {

        requestService.deleteRequestById(requestId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{requestId}/accept")
    @RolesAllowed({ Role.Constants.USER })
    public Response acceptRequest(@PathParam("requestId") Integer requestId)
            throws ForbiddenActionException, RequestNotFoundException {

        requestService.acceptRequest(requestId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{requestId}/reject")
    @RolesAllowed({ Role.Constants.USER })
    public Response rejectRequest(@PathParam("requestId") Integer requestId)
            throws ForbiddenActionException, RequestNotFoundException {

        requestService.rejectRequest(requestId);
        return Response.noContent().build();
    }

}
