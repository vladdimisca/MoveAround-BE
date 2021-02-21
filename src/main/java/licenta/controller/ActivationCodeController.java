package licenta.controller;

import licenta.exception.definition.InternalServerErrorException;
import licenta.exception.definition.UserNotFoundException;
import licenta.exception.definition.WrongActivationCodeException;
import licenta.model.ActivationCode;
import licenta.model.User;
import licenta.service.ActivationCodeService;
import licenta.service.SmsService;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/activation-code")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivationCodeController {

    @Inject
    ActivationCodeService activationCodeService;

    @Inject
    SmsService smsService;

    @POST
    @PermitAll
    @Path("/email-activation/{userId}")
    public Response activateEmailByUserId(@PathParam("userId") UUID userId, ActivationCode activationCode)
            throws InternalServerErrorException, WrongActivationCodeException, UserNotFoundException {

        User user = new User();
        user.setCallingCode("");
        user.setPhoneNumber("");
        smsService.sendConfirmationSms(user, "");
        //activationCodeService.verifyCodeAndEnableEmailById(userId, activationCode.getEmailCode());
        return Response.noContent().build();
    }
}
