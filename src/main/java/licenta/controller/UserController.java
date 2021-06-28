package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.*;
import licenta.exception.definition.InternalServerErrorException;
import licenta.mapper.UserMapper;
import licenta.model.ActivationCode;
import licenta.model.User;
import licenta.service.UserService;

import licenta.util.enumeration.Role;
import org.jose4j.json.internal.json_simple.JSONObject;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserController {

    @Inject
    UserService userService;

    @Inject
    UserMapper userMapper;

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

        userService.updatePasswordById(userId, (String)body.get("oldPassword"), (String)body.get("newPassword"));
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{userId}/profile-picture")
    @Authenticated
    public Response updateProfilePictureById(@PathParam("userId") UUID userId, JSONObject profilePictureBody)
            throws ForbiddenActionException, InternalServerErrorException,
            FailedToParseTheBodyException, UserNotFoundException {

        String profilePicture = (String) profilePictureBody.get("profilePicture");

        return Response.ok(userMapper.fromUser(
                        userService.updateProfilePictureById(userId, profilePicture))).build();
    }

    @PUT
    @Path("/{userId}")
    @RolesAllowed({ Role.Constants.USER })
    public Response updateUserById(@PathParam("userId") UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException,
            EmailAlreadyExistsException, PhoneNumberAlreadyExistsException, InternalServerErrorException {

        userService.updateUserById(userId, user);
        return Response.ok(userMapper.fromUser(userService.getUserById(userId))).build();
    }

    @DELETE
    @Path("/{userId}")
    @Authenticated
    public Response deleteUserById(@PathParam("userId") UUID userId, @HeaderParam("Password") String password)
            throws ForbiddenActionException, UserNotFoundException, InternalServerErrorException {

        userService.deleteUserById(userId, password);
        return Response.noContent().build();
    }

    @POST
    @Path("/{email}/forgot-password")
    public Response forgotPassword(@PathParam("email") String email)
            throws UserNotFoundException, InternalServerErrorException, ForbiddenActionException {

        userService.sendNewPasswordByEmail(email);
        return Response.ok("A new password has been sent to you by email.").build();
    }

    @POST
    @RolesAllowed({ Role.Constants.USER })
    @Path("/{userId}/activation/email")
    public Response activateEmailByUserId(@PathParam("userId") UUID userId, ActivationCode activationCode)
            throws InternalServerErrorException, WrongActivationCodeException, ForbiddenActionException,
            ActivationCodeNotFoundException, ActivationCodeExpiredException, UserNotFoundException {

        userService.verifyCodeAndEnableEmailById(userId, activationCode.getEmailCode());
        return Response.noContent().build();
    }

    @POST
    @RolesAllowed({ Role.Constants.USER })
    @Path("/{userId}/activation/email/resend")
    public Response resendEmailCodeByUserId(@PathParam("userId") UUID userId) throws UserNotFoundException,
            InternalServerErrorException, ActivationCodeNotFoundException, ForbiddenActionException {

        userService.resendEmailCodeById(userId);
        return Response.noContent().build();
    }

    @POST
    @RolesAllowed({ Role.Constants.USER })
    @Path("/{userId}/activation/phone")
    public Response activatePhoneByUserId(@PathParam("userId") UUID userId, ActivationCode activationCode)
            throws InternalServerErrorException, WrongActivationCodeException, UserNotFoundException,
            ActivationCodeNotFoundException, ActivationCodeExpiredException, ForbiddenActionException {

        userService.verifyCodeAndEnablePhoneById(userId, activationCode.getSmsCode());
        return Response.noContent().build();
    }

    @POST
    @RolesAllowed({ Role.Constants.USER })
    @Path("/{userId}/activation/phone/resend")
    public Response resendSmsCodeByUserId(@PathParam("userId") UUID userId) throws UserNotFoundException,
            InternalServerErrorException, ActivationCodeNotFoundException, ForbiddenActionException {

        userService.resendSmsCodeById(userId);
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({ Role.Constants.ADMIN })
    public Response getAllUsers() {
        return Response.ok(
                userService.getAllUsers().stream().map(userMapper::fromUser).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/join-statistics")
    @RolesAllowed({ Role.Constants.ADMIN })
    public Response getJoinStatistics() {
        return Response.ok(userService.getJoinStatistics()).build();
    }

}
