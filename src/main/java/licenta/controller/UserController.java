package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.*;
import licenta.exception.definition.InternalServerErrorException;
import licenta.mapper.UserMapper;
import licenta.model.ActivationCode;
import licenta.model.User;
import licenta.service.UserService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.json.internal.json_simple.JSONObject;

import javax.annotation.security.PermitAll;
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
            PhoneNumberAlreadyExistsException, InternalServerErrorException {

        return Response.ok().entity(userMapper.fromUser(userService.createUser(user))).build();
    }

    @POST
    @PermitAll
    @Path("/login")
    public Response login(User user) throws InternalServerErrorException, UserNotFoundException,
            WrongPasswordException {
        return userService.verifyUserAndGenerateToken(user.getPhoneNumber(), user.getCallingCode(), user.getPassword());
    }

    @GET
    @Path("/{userId}")
    @Authenticated
    public Response getUserById(@PathParam("userId") UUID userId) throws UserNotFoundException {
        return Response.ok().entity(userMapper.fromUser(userService.getUserById(userId))).build();
    }

    @PATCH
    @Path("/{userId}/password")
    @Authenticated
    public Response updatePasswordById(@PathParam("userId") UUID userId, JSONObject body)
            throws UserNotFoundException, WrongPasswordException, ForbiddenActionException,
            FailedToParseTheBodyException, InternalServerErrorException {

        userService.updatePasswordById(userId, (String)body.get("oldPassword"), (String)body.get("newPassword"), jwt);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/profile-picture")
    @Authenticated
    public Response updateProfilePictureById(@PathParam("userId") UUID userId, String profilePicture)
            throws ForbiddenActionException, InternalServerErrorException,
            FailedToParseTheBodyException, UserNotFoundException {

        return Response.ok(userMapper.fromUser(
                        userService.updateProfilePictureById(userId, profilePicture, jwt))).build();
    }

    @PUT
    @Path("/{userId}")
    @Authenticated
    public Response updateUserById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException,
            EmailAlreadyExistsException, PhoneNumberAlreadyExistsException, InternalServerErrorException {

        userService.updateUserById(userId, user, jwt);
        return Response.ok(userMapper.fromUser(userService.getUserById(userId))).build();
    }

    @DELETE
    @Path("/{userId}")
    @Authenticated
    public Response deleteUserById(@PathParam("userId") UUID userId)
            throws ForbiddenActionException, UserNotFoundException {

        userService.deleteUserById(userId, jwt);
        return Response.noContent().build();
    }

    @POST
    @Path("/{email}/forgot-password")
    public Response forgotPassword(@PathParam("email") String email)
            throws UserNotFoundException, InternalServerErrorException {

        userService.sendNewPasswordByEmail(email);
        return Response.ok("A new password has been sent to you by email.").build();
    }

    @POST
    @Authenticated
    @Path("/{userId}/activation/email")
    public Response activateEmailByUserId(@PathParam("userId") UUID userId, ActivationCode activationCode)
            throws InternalServerErrorException, WrongActivationCodeException, UserNotFoundException,
            ActivationCodeNotFoundException, ActivationCodeExpiredException, ForbiddenActionException {

        userService.verifyCodeAndEnableEmailById(userId, activationCode.getEmailCode(), jwt);
        return Response.noContent().build();
    }

    @POST
    @Authenticated
    @Path("/{userId}/activation/email/resend")
    public Response resendEmailCodeByUserId(@PathParam("userId") UUID userId) throws UserNotFoundException,
            InternalServerErrorException, ActivationCodeNotFoundException, ForbiddenActionException {

        userService.resendEmailCodeById(userId, jwt);
        return Response.noContent().build();
    }
}
