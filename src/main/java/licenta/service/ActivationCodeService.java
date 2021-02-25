package licenta.service;

import licenta.dao.ActivationCodeDAO;
import licenta.dao.UserDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.InternalServerErrorException;
import licenta.exception.definition.UserNotFoundException;
import licenta.exception.definition.WrongActivationCodeException;
import licenta.model.ActivationCode;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ApplicationScoped
public class ActivationCodeService {

    @Inject
    ActivationCodeDAO activationCodeDAO;

    @Inject
    EncryptionService encryptionService;

    @Inject
    UserDAO userDAO;

    @Inject
    SmsService smsService;

    @Inject
    UserService userService;

    @Transactional
    public void verifyCodeAndEnableEmailById(UUID userId, String codeGuess)
            throws InternalServerErrorException, WrongActivationCodeException, UserNotFoundException {

        ActivationCode activationCode = userService.getUserById(userId).getActivationCode();
        try {
            if (!encryptionService.decryptAES(activationCode.getEmailCode()).equals(codeGuess)) {
                throw new WrongActivationCodeException(
                        ExceptionMessage.WRONG_ACTIVATION_CODE, Response.Status.FORBIDDEN);
            }
            userDAO.updateEmailEnabledById(userId, true);
        } catch (NoSuchAlgorithmException | InternalServerErrorException | InvalidKeyException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {

            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void verifyCodeAndEnablePhoneById(UUID userId, String codeGuess) {

    }
}
