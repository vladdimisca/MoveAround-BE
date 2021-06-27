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
import licenta.util.enumeration.Configuration;
import licenta.util.enumeration.Role;
import licenta.validator.UserValidator;
import licenta.validator.ValidationMode;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.json.internal.json_simple.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Inject
    JsonWebToken jwt;

    private void checkEmailNotUsed(String email) throws EmailAlreadyExistsException {
        if (userDAO.getUserByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(ExceptionMessage.EMAIL_ALREADY_EXISTS, Response.Status.CONFLICT);
        }
    }

    private void checkPhoneNumberNotUsed(String phoneNumber, String callingCode) throws PhoneNumberAlreadyExistsException {
        if (userDAO.getUserByFullPhoneNumber(phoneNumber, callingCode).isPresent()) {
            throw new PhoneNumberAlreadyExistsException(
                    ExceptionMessage.PHONE_NUMBER_ALREADY_EXISTS, Response.Status.CONFLICT);
        }
    }

    public void checkUserExistenceById(UUID userId) throws UserNotFoundException {
        if (userDAO.getUserById(userId).isEmpty()) {
            throw new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    public void updateEmailActivationCode(ActivationCode activationCode, String emailCode)
            throws InternalServerErrorException {

        activationCode.setEmailCreatedAt(java.util.Calendar.getInstance().getTime());

        try {
            activationCode.setEmailCode(encryptionService.encryptAES(emailCode));
        } catch (Exception e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void updateSmsActivationCode(ActivationCode activationCode, String smsCode)
            throws InternalServerErrorException {

        activationCode.setSmsCreatedAt(java.util.Calendar.getInstance().getTime());

        try {
            activationCode.setSmsCode(encryptionService.encryptAES(smsCode));
        } catch (Exception e) {
            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public User createUser(User user) throws EmailAlreadyExistsException, PhoneNumberAlreadyExistsException,
            InternalServerErrorException, FailedToParseTheBodyException {

        userValidator.validate(user, ValidationMode.CREATE);
        checkEmailNotUsed(user.getEmail());
        checkPhoneNumberNotUsed(user.getPhoneNumber(), user.getCallingCode());

        String emailCode = SecureRandomService.generateRandomCode();
        String smsCode = SecureRandomService.generateRandomCode();

        ActivationCode activationCode = new ActivationCode();
        updateEmailActivationCode(activationCode, emailCode);
        updateSmsActivationCode(activationCode, smsCode);

        activationCodeDAO.persist(activationCode);

        user.setId(UUID.randomUUID());
        user.setRole(Role.USER.getValue());
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(encryptionService.hash(user.getPassword()));
        user.setActivationCode(activationCode);
        user.setProfilePictureURL(null);
        user.setDescription("");
        user.setEmailEnabled(false);
        user.setPhoneEnabled(false);
        userDAO.persist(user);

        // send an email with the activation code
        emailService.sendConfirmationEmail(user, emailCode);

        // send a sms with the activation code
        smsService.sendConfirmationSms(user, smsCode);

        return user;
    }

    public User getUserById(UUID userId) throws UserNotFoundException {
        Optional<User> user = userDAO.getUserById(userId);
        return user.orElseThrow(() ->
                new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserByFullPhoneNumber(String phoneNumber, String callingCode) throws UserNotFoundException {
        Optional<User> user = userDAO.getUserByFullPhoneNumber(phoneNumber, callingCode);
        return user.orElseThrow(() ->
                new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    public Response verifyUserAndGenerateToken(String phoneNumber, String callingCode, String password)
            throws UserNotFoundException, InternalServerErrorException, WrongPasswordException {

        User user;
        try {
            user = getUserByFullPhoneNumber(phoneNumber, callingCode);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(ExceptionMessage.AUTH_ERROR, Response.Status.FORBIDDEN);
        }

        if (!encryptionService.passwordMatchesHash(user.getPassword(), password)) {
            throw new WrongPasswordException(ExceptionMessage.AUTH_ERROR, Response.Status.FORBIDDEN);
        }

        String token = jwtService.generateJwt(user.getEmail(), user.getId(), user.getRole());

        return Response
                .ok(userMapper.fromUser(user))
                .header(Authentication.HEADER_NAME.getValue(), Authentication.TOKEN_PREFIX.getValue() + token)
                .build();
    }

    @Transactional
    public void updateUserById(UUID userId, User user)
            throws ForbiddenActionException, FailedToParseTheBodyException, EmailAlreadyExistsException,
            UserNotFoundException, PhoneNumberAlreadyExistsException, InternalServerErrorException {

        checkUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId);
        userValidator.validate(user, ValidationMode.UPDATE);

        boolean isEmailChanged = false;
        boolean isPhoneNumberChanged = false;

        User existingUser = getUserById(userId);
        if (!existingUser.getEmail().equals(user.getEmail())) {
            checkEmailNotUsed(user.getEmail());
            existingUser.setEmailEnabled(false);
            isEmailChanged = true;
        }
        if (!existingUser.getCallingCode().equals(user.getCallingCode()) ||
                !existingUser.getPhoneNumber().equals(user.getPhoneNumber())) {
            checkPhoneNumberNotUsed(user.getPhoneNumber(), user.getCallingCode());
            existingUser.setPhoneEnabled(false);
            isPhoneNumberChanged = true;
        }

        existingUser.setCallingCode(user.getCallingCode());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setDescription(user.getDescription());
        userDAO.persist(existingUser);

        if (isEmailChanged) {
            String emailCode = SecureRandomService.generateRandomCode();
            updateEmailActivationCode(existingUser.getActivationCode(), emailCode);
            activationCodeDAO.persist(existingUser.getActivationCode());
            emailService.sendConfirmationEmail(existingUser, emailCode);
        }

        if (isPhoneNumberChanged) {
            String smsCode = SecureRandomService.generateRandomCode();
            updateSmsActivationCode(existingUser.getActivationCode(), smsCode);
            activationCodeDAO.persist(existingUser.getActivationCode());
            smsService.sendConfirmationSms(existingUser, smsCode);
        }
    }

    @Transactional
    public void updatePasswordById(UUID userId, String oldPassword, String newPassword)
            throws ForbiddenActionException, FailedToParseTheBodyException, WrongPasswordException,
            UserNotFoundException, InternalServerErrorException {

        checkUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId);

        User persistedUser = getUserById(userId);

        if (!encryptionService.passwordMatchesHash(persistedUser.getPassword(), oldPassword)) {
            throw new WrongPasswordException(ExceptionMessage.WRONG_PASSWORD, Response.Status.FORBIDDEN);
        }

        userValidator.validatePassword(newPassword);
        userDAO.updatePasswordById(userId, encryptionService.hash(newPassword));
    }

    @Transactional
    public User updateProfilePictureById(UUID userId, String base64image) throws ForbiddenActionException,
            InternalServerErrorException, FailedToParseTheBodyException, UserNotFoundException {

        checkUserExistenceById(userId);
        checkIfUserIdMatchesToken(userId);
        userValidator.validateProfilePicture(base64image);

        Blob blob = StorageUtil
                .getDefaultBucket()
                .create(userId.toString(), Base64.getDecoder().decode(base64image), "image");

        BlobInfo blobInfo = BlobInfo
                .newBuilder(Util.getValueOfConfigVariable(Configuration.BUCKET_NAME), blob.getName()).build();

        URL signedURL = StorageUtil
                .getDefaultBucket()
                .getStorage()
                .signUrl(blobInfo, 365 * 10, TimeUnit.DAYS);

        userDAO.updateProfilePictureURLById(userId, signedURL.toString());

        User user = new User();
        user.setProfilePictureURL(signedURL.toString());
        return user;
    }

    @Transactional
    public void deleteUserById(UUID userId, String password)
            throws ForbiddenActionException, UserNotFoundException, InternalServerErrorException {

        isAdminOrOwnsAccount(userId);

        User userToDelete = getUserById(userId);
        User caller = getUserById(UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue())));

        if (userToDelete.getRole().equals(Role.ADMIN.getValue())) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "Cannot delete an admin account");
        }
        if (!encryptionService.passwordMatchesHash(caller.getPassword(), password)) {
            throw new ForbiddenActionException(ExceptionMessage.WRONG_PASSWORD, Response.Status.FORBIDDEN);
        }

        userDAO.delete(userToDelete);
        activationCodeDAO.delete(userToDelete.getActivationCode());
    }

    public void checkIfUserIdMatchesToken(UUID userId) throws ForbiddenActionException {
        if (!userId.equals(UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue())))) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "User id does not match the token");
        }
    }

    public void isAdminOrOwnsAccount(UUID userId) throws ForbiddenActionException {
        try {
            checkIfUserIdMatchesToken(userId);
        } catch (ForbiddenActionException e) {
            if (!jwt.getGroups().contains(Role.ADMIN.getValue())) {
                throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                        Response.Status.FORBIDDEN, "You must either be admin or own this account");
            }
        }
    }

    @Transactional
    public void sendNewPasswordByEmail(String email)
            throws UserNotFoundException, InternalServerErrorException, ForbiddenActionException {

        User user = userDAO.getUserByEmail(email).orElseThrow(() ->
                new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND, Response.Status.NOT_FOUND));

        checkIfUserIdMatchesToken(user.getId());

        String newPassword = SecureRandomService.generateRandomPassword();
        user.setPassword(encryptionService.hash(newPassword));

        userDAO.persist(user);
        emailService.sendNewPassword(user, newPassword);
    }

    @Transactional
    public void verifyCodeAndEnableEmailById(UUID userId, String codeGuess)
            throws InternalServerErrorException, WrongActivationCodeException, UserNotFoundException,
            ActivationCodeNotFoundException, ActivationCodeExpiredException, ForbiddenActionException {

        checkIfUserIdMatchesToken(userId);

        User user = getUserById(userId);
        ActivationCode activationCode = user.getActivationCode();

        if (activationCode == null || activationCode.getEmailCode() == null) {
            throw new ActivationCodeNotFoundException(ExceptionMessage.ACTIVATION_CODE_NOT_FOUND,
                    Response.Status.NOT_FOUND, "There is no activation code generated for this email");
        }
        try {
            if (!encryptionService.decryptAES(activationCode.getEmailCode()).equals(codeGuess)) {
                throw new WrongActivationCodeException(
                        ExceptionMessage.WRONG_ACTIVATION_CODE, Response.Status.FORBIDDEN);
            }

            long passedMilliseconds = (new java.util.Date()).getTime() - activationCode.getEmailCreatedAt().getTime();
            long difference = TimeUnit.MINUTES.convert(passedMilliseconds , TimeUnit.MILLISECONDS);

            if (difference > 3) {
                throw new ActivationCodeExpiredException(
                        ExceptionMessage.ACTIVATION_CODE_EXPIRED, Response.Status.FORBIDDEN);
            }
            user.setEmailEnabled(true);
        } catch (NoSuchAlgorithmException | InternalServerErrorException | InvalidKeyException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {

            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void resendEmailCodeById(UUID userId) throws UserNotFoundException, ActivationCodeNotFoundException,
            InternalServerErrorException, ForbiddenActionException {

        checkIfUserIdMatchesToken(userId);

        User user = getUserById(userId);
        ActivationCode activationCode = user.getActivationCode();

        if (activationCode == null || activationCode.getEmailCode() == null) {
            throw new ActivationCodeNotFoundException(ExceptionMessage.ACTIVATION_CODE_NOT_FOUND,
                    Response.Status.NOT_FOUND, "There is no activation code sent for this email");
        }

        String newEmailCode = SecureRandomService.generateRandomCode();
        try {
            user.getActivationCode().setEmailCode(encryptionService.encryptAES(newEmailCode));
            user.getActivationCode().setEmailCreatedAt(java.util.Calendar.getInstance().getTime());

            activationCodeDAO.persist(user.getActivationCode());
            emailService.sendConfirmationEmail(user, newEmailCode);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException |
                InvalidKeyException | InternalServerErrorException e) {

            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void verifyCodeAndEnablePhoneById(UUID userId, String codeGuess)
            throws ActivationCodeExpiredException, UserNotFoundException, ActivationCodeNotFoundException,
            WrongActivationCodeException, ForbiddenActionException, InternalServerErrorException {

        checkIfUserIdMatchesToken(userId);

        User user = getUserById(userId);
        ActivationCode activationCode = user.getActivationCode();

        if (activationCode == null || activationCode.getSmsCode() == null) {
            throw new ActivationCodeNotFoundException(ExceptionMessage.ACTIVATION_CODE_NOT_FOUND,
                    Response.Status.NOT_FOUND, "There is no activation code generated for this phone number");
        }

        try {
            if (!encryptionService.decryptAES(activationCode.getSmsCode()).equals(codeGuess)) {
                throw new WrongActivationCodeException(
                        ExceptionMessage.WRONG_ACTIVATION_CODE, Response.Status.FORBIDDEN);
            }

            long passedMilliseconds = (new java.util.Date()).getTime() - activationCode.getSmsCreatedAt().getTime();
            long difference = TimeUnit.MINUTES.convert(passedMilliseconds , TimeUnit.MILLISECONDS);

            if (difference > 3) {
                throw new ActivationCodeExpiredException(
                        ExceptionMessage.ACTIVATION_CODE_EXPIRED, Response.Status.FORBIDDEN);
            }
            user.setPhoneEnabled(true);
        } catch (NoSuchAlgorithmException | InternalServerErrorException | InvalidKeyException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {

            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void resendSmsCodeById(UUID userId) throws ForbiddenActionException, UserNotFoundException,
            ActivationCodeNotFoundException, InternalServerErrorException {

        checkIfUserIdMatchesToken(userId);

        User user = getUserById(userId);
        ActivationCode activationCode = user.getActivationCode();

        if (activationCode == null || activationCode.getSmsCode() == null) {
            throw new ActivationCodeNotFoundException(ExceptionMessage.ACTIVATION_CODE_NOT_FOUND,
                    Response.Status.NOT_FOUND, "There is no activation code sent for this phone number");
        }

        String newSmsCode = SecureRandomService.generateRandomCode();
        try {
            user.getActivationCode().setSmsCode(encryptionService.encryptAES(newSmsCode));
            user.getActivationCode().setSmsCreatedAt(java.util.Calendar.getInstance().getTime());

            activationCodeDAO.persist(user.getActivationCode());
            smsService.sendConfirmationSms(user, newSmsCode);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException |
                InvalidKeyException | InternalServerErrorException e) {

            throw new InternalServerErrorException(
                    ExceptionMessage.INTERNAL_SERVER_ERROR, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJoinStatistics() {
        final String[] allMonths = {
                "January", "February", "March", "April", "May", "June", "July", "August", "September", "October",
                "November", "December"
        };

        List<User> users = getAllUsers();
        List<String> months = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        LocalDateTime currentDateTime = LocalDateTime.now();

        for (int month = 1; month <= currentDateTime.getMonthValue(); month++) {
            final int finalMonth = month; // variables used in lambda expression should be final or effectively final

            var newUsers = users.stream()
                    .filter(user -> user.getCreatedAt().getYear() == currentDateTime.getYear() &&
                            user.getCreatedAt().getMonthValue() == finalMonth)
                    .collect(Collectors.toList());

            months.add(allMonths[finalMonth - 1]);
            values.add(newUsers.size());
        }

        JSONObject joinStats = new JSONObject();
        joinStats.put("labels", months);
        joinStats.put("data", values);

        return joinStats;
    }

}
