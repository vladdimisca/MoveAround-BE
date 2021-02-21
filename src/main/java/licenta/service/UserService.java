package licenta.service;

import com.google.cloud.storage.*;
import licenta.dao.ActivationCodeDAO;
import licenta.dao.UserDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.*;
import licenta.mapper.UserMapper;
import licenta.model.ActivationCode;
import licenta.model.User;
import licenta.util.StorageUtil;
import licenta.util.Util;
import licenta.util.enumeration.Authentication;
import licenta.util.enumeration.Environment;
import licenta.validator.UserValidator;
import licenta.validator.ValidationMode;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserService {

    @Inject
    UserDAO userDAO;

    @Inject
    UserMapper userMapper;

    @Inject
    EncryptionService encryptionService;

    @Inject
    UserValidator userValidator;

    @Inject
    JwtService jwtService;

    @Inject
    ActivationCodeDAO activationCodeDAO;

    @Inject
    EmailService emailService;

    @Inject
    SmsService smsService;

    private void checkEmailNotUsed(String email, String role) throws EmailAlreadyExistsException, RoleNotFoundException {
        if (userDAO.getUserByEmail(email, Util.getUserRoleFromValue(role)).isPresent()) {
            throw new EmailAlreadyExistsException(ExceptionMessage.EMAIL_ALREADY_EXISTS, Response.Status.CONFLICT);
        }
    }

    private void checkPhoneNumberNotUsed(String phoneNumber, String callingCode, String role)
            throws PhoneNumberAlreadyExistsException, RoleNotFoundException {

        if (userDAO.getUserByFullPhoneNumber(phoneNumber, callingCode, Util.getUserRoleFromValue(role)).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(
                    ExceptionMessage.PHONE_NUMBER_ALREADY_EXISTS, Response.Status.CONFLICT);
        }
    }

    @Transactional
    public User createUser(User user) throws EmailAlreadyExistsException, PhoneNumberAlreadyExistsException,
            InternalServerErrorException, FailedToParseTheBodyException, RoleNotFoundException {

        userValidator.validate(user, ValidationMode.CREATE);
        checkEmailNotUsed(user.getEmail(), Util.getUserRoleFromValue(user.getRole()).getValue());
        checkPhoneNumberNotUsed(
                user.getPhoneNumber(), user.getCallingCode(), Util.getUserRoleFromValue(user.getRole()).getValue());

        String emailCode = SecureRandomService.generateRandomCode();
        ActivationCode activationCode = new ActivationCode();
        try {
            activationCode.setEmailCode(encryptionService.encryptAES(emailCode));
        } catch (Exception e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }

        activationCodeDAO.persist(activationCode);

        user.setId(UUID.randomUUID());
        user.setCreatedAt(java.util.Calendar.getInstance().getTime());
        user.setRole(Util.getUserRoleFromValue(user.getRole()).getValue());
        user.setPassword(encryptionService.encrypt(user.getPassword())); // encrypt the password
        user.setActivationCode(activationCode);
        user.setProfilePictureURL(null);
        user.setEmailEnabled(false);
        user.setPhoneEnabled(false);
        userDAO.persist(user);

        // send an email with the activation code
        emailService.sendConfirmationEmail(user, emailCode);

        return user;
    }

    public User getUserById(UUID userId) throws UserNotFoundException {
        Optional<User> user = userDAO.getUserById(userId);
        return user.orElseThrow(() ->
                new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public User getUserByFullPhoneNumber(String phoneNumber, String callingCode, String role)
            throws UserNotFoundException, RoleNotFoundException {

        Optional<User> user = userDAO.getUserByFullPhoneNumber(phoneNumber, callingCode, Util.getUserRoleFromValue(role));
        return user.orElseThrow(() ->
                new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public Response verifyUserAndGenerateToken(String phoneNumber, String callingCode, String role, String password)
            throws UserNotFoundException, InternalServerErrorException, WrongPasswordException, RoleNotFoundException {

        userValidator.validateRole(role);

        User user = getUserByFullPhoneNumber(phoneNumber, callingCode, role);
        if (!encryptionService.passwordMatchesHash(user.getPassword(), password)) {
            throw new WrongPasswordException(ExceptionMessage.WRONG_PASSWORD, Response.Status.FORBIDDEN);
        }
        String token = jwtService.
                generateJwt(user.getEmail(), user.getId(), Util.generateSingleRoleSetFromValue(user.getRole()));

        return Response
                .ok(userMapper.fromUser(user))
                .header(Authentication.HEADER_NAME.getValue(), Authentication.TOKEN_PREFIX.getValue() + token)
                .build();
    }

    public void verifyUserExistenceById(UUID userId) throws UserNotFoundException {
        if (userDAO.getUserById(userId).isEmpty()) {
            throw new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    @Transactional
    public void updateEmailById(UUID userId, String email, JsonWebToken jwt)
            throws ForbiddenActionException, FailedToParseTheBodyException, EmailAlreadyExistsException,
            UserNotFoundException, InternalServerErrorException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        User existingUser = getUserById(userId);
        if (existingUser.getEmail().equals(email)) {
            return;
        }
        try {
            checkEmailNotUsed(email, existingUser.getRole());
        } catch (RoleNotFoundException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
        userValidator.validateEmail(email);
        userDAO.updateEmailById(userId, email);
    }

    @Transactional
    public void updateFirstNameById(UUID userId, String firstName, JsonWebToken jwt)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        userValidator.validateFirstName(firstName);
        userDAO.updateFirstNameById(userId, firstName);
    }

    @Transactional
    public void updateLastNameById(UUID userId, String lastName, JsonWebToken jwt)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        userValidator.validateLastName(lastName);
        userDAO.updateLastNameById(userId, lastName);
    }

    @Transactional
    public void updateFullPhoneNumberById(UUID userId, String phoneNumber, String callingCode, JsonWebToken jwt)
            throws ForbiddenActionException, FailedToParseTheBodyException, UserNotFoundException,
            PhoneNumberAlreadyExistsException, InternalServerErrorException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        User existingUser = getUserById(userId);
        if (existingUser.getPhoneNumber().equals(phoneNumber) && existingUser.getCallingCode().equals(callingCode)) {
            return;
        }
        try {
            checkPhoneNumberNotUsed(phoneNumber, callingCode, existingUser.getRole());
        } catch (RoleNotFoundException e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
        userValidator.validateFullPhoneNumber(phoneNumber, callingCode);
        userDAO.updateFullPhoneNumberById(userId, phoneNumber, callingCode);
    }

    @Transactional
    public void updatePasswordById(UUID userId, String oldPassword, String newPassword, JsonWebToken jwt)
            throws ForbiddenActionException, FailedToParseTheBodyException, WrongPasswordException,
            UserNotFoundException, InternalServerErrorException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        User persistedUser = getUserById(userId);
        if (!encryptionService.passwordMatchesHash(persistedUser.getPassword(), oldPassword)) {
            throw new WrongPasswordException(ExceptionMessage.WRONG_PASSWORD, Response.Status.FORBIDDEN);
        }
        userValidator.validatePassword(newPassword);
        userDAO.updatePasswordById(userId, encryptionService.encrypt(newPassword));
    }

    @Transactional
    public User updateProfilePictureById(UUID userId, String image, JsonWebToken jwt) throws ForbiddenActionException,
            InternalServerErrorException, FailedToParseTheBodyException, UserNotFoundException {

        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        userValidator.validateProfilePictureNotNull(image);

        String mimeType;
        Matcher matcher = Pattern.compile("data:([a-zA-Z0-9]+\\/[a-zA-Z0-9-.+]+).*,.*").matcher(image);
        if (matcher.find()) {
            mimeType = matcher.group(1);
        } else {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Bad image format");
        }
        String base64EncodedImageString = image.replace("data:" + mimeType + ";base64,", "");
        Blob blob = StorageUtil
                .getDefaultBucket()
                .create(userId.toString(), Base64.getDecoder().decode(base64EncodedImageString), mimeType);

        BlobInfo blobInfo = BlobInfo
                .newBuilder(Util.getValueOfEnvironmentVariable(Environment.BUCKET_NAME), blob.getName()).build();

        URL signedURL = StorageUtil
                .getDefaultBucket()
                .getStorage()
                .signUrl(blobInfo, 365 * 10, TimeUnit.DAYS);

        User user = new User();
        user.setProfilePictureURL(signedURL.toString());
        userDAO.updateProfilePictureURLById(userId, signedURL.toString());

        return user;
    }

    @Transactional
    public void deleteUserById(UUID userId, JsonWebToken jwt) throws ForbiddenActionException, UserNotFoundException {
        verifyUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId, jwt);
        userDAO.removeUserById(userId);
    }

    private void checkIfUserIdMatchesToken(UUID userId, JsonWebToken jwt) throws ForbiddenActionException {
        if (!userId.equals(UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue())))) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "This id does not match the token");
        }
    }
}
