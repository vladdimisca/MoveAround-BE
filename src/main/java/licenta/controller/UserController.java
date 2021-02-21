package licenta.controller;

import licenta.exception.definition.*;
import licenta.exception.definition.InternalServerErrorException;
import licenta.mapper.UserMapper;
import licenta.model.User;
import licenta.service.UserService;
import licenta.util.enumeration.UserRole;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.json.internal.json_simple.JSONObject;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserController {

    @Inject
    UserService userService;

    @Inject
    UserMapper userMapper;

    @Inject
    JsonWebToken jwt;

    @POST
    @PermitAll
    @Path("/register")
    public Response register(User user) throws FailedToParseTheBodyException, EmailAlreadyExistsException,
            PhoneNumberAlreadyExistsException, InternalServerErrorException, RoleNotFoundException {

        return Response.ok().entity(userMapper.fromUser(userService.createUser(user))).build();
    }

    @POST
    @PermitAll
    @Path("/login")
    public Response login(User user) throws InternalServerErrorException, UserNotFoundException,
            WrongPasswordException, RoleNotFoundException {

        return userService.verifyUserAndGenerateToken(
                user.getPhoneNumber(), user.getCallingCode(), user.getRole(), user.getPassword());
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response getUserById(@PathParam("userId") UUID userId) throws UserNotFoundException {
        return Response.ok().entity(userMapper.fromUser(userService.getUserById(userId))).build();
    }


    @PATCH
    @Path("/{userId}/password")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updatePasswordById(@PathParam("userId") UUID userId, JSONObject body)
            throws UserNotFoundException, WrongPasswordException, ForbiddenActionException,
            FailedToParseTheBodyException, InternalServerErrorException {

        userService.updatePasswordById(userId, (String)body.get("oldPassword"), (String)body.get("newPassword"), jwt);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/profile-picture")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updateProfilePictureById(@PathParam("userId") UUID userId, String profilePicture)
            throws ForbiddenActionException, InternalServerErrorException,
            FailedToParseTheBodyException, UserNotFoundException {

        return Response.ok(userMapper.fromUser(
                        userService.updateProfilePictureById(userId, profilePicture, jwt))).build();
    }

    @PATCH
    @Path("/{userId}/email")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updateEmailById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException,
            EmailAlreadyExistsException, InternalServerErrorException {

        userService.updateEmailById(userId, user.getEmail(), jwt);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/first-name")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updateFirstNameById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException {

        userService.updateFirstNameById(userId,user.getFirstName(), jwt);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/last-name")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updateLastNameById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException {

        userService.updateLastNameById(userId,user.getLastName(), jwt);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/phone-number")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response updateFullPhoneNumberById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException,
            InternalServerErrorException, PhoneNumberAlreadyExistsException {

        userService.updateFullPhoneNumberById(userId, user.getPhoneNumber(), user.getCallingCode(), jwt);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{userId}")
    @RolesAllowed(value = {UserRole.Constants.DRIVER_VALUE, UserRole.Constants.PASSENGER_VALUE})
    public Response deleteUserById(@PathParam("userId") UUID userId)
            throws ForbiddenActionException, UserNotFoundException {

        userService.deleteUserById(userId, jwt);
        return Response.noContent().build();
    }
}
