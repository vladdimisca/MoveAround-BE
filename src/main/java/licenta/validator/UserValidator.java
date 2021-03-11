package licenta.validator;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.model.User;
import org.apache.commons.codec.binary.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class UserValidator implements Validator<User> {
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,20}$";

    @Override
    public void validate(User user, ValidationMode validationMode)
            throws FailedToParseTheBodyException {

        validateEmail(user.getEmail());
        validateFirstName(user.getFirstName());
        validateLastName(user.getLastName());
        validateFullPhoneNumber(user.getPhoneNumber(), user.getCallingCode());
        if (validationMode.equals(ValidationMode.CREATE)) {
            validatePassword(user.getPassword());
        }
        if (validationMode.equals(ValidationMode.UPDATE)) {
            validateDescription(user.getDescription());
        }
    }

    public void validateEmail(String email) throws FailedToParseTheBodyException {
        if (email == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Email is missing");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "This email address is not valid");
        }
    }

    public void validateFirstName(String firstName) throws FailedToParseTheBodyException {
        if (firstName == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "First name is missing");
        }
        if (firstName.length() < 2 || firstName.length() > 30) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "First name must be between 2 and 30 characters long");
        }
    }

    public void validateLastName(String lastName) throws FailedToParseTheBodyException {
        if (lastName == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Last name is missing");
        }
        if (lastName.length() < 2 || lastName.length() > 30) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Last name must be between 2 and 30 characters long");
        }
    }

    public void validateDescription(String description) throws FailedToParseTheBodyException {
        if (description == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Description is missing");
        }
        if (description.length() > 100) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Description must be at most 100 characters long");
        }
    }

    public void validatePassword(String password) throws FailedToParseTheBodyException {
        if (password == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Password is missing");
        }
        final String passwordErrorMessage =
                "Password must be between 8 and 20 characters long and must contain at least one lowercase, " +
                "one uppercase and one digit";
        if (!password.matches(PASSWORD_PATTERN)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, passwordErrorMessage);
        }
    }

    public void validateFullPhoneNumber(String phoneNumber, String callingCode) throws FailedToParseTheBodyException {
        if (phoneNumber == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Phone number is missing");
        }
        if (callingCode == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Calling code is missing");
        }
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        try {
            number.setCountryCode(Integer.parseInt(callingCode)).setNationalNumber(Long.parseLong(phoneNumber));
        } catch (RuntimeException e) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Invalid phone number");
        }
        if (!phoneNumberUtil.isValidNumber(number)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Invalid phone number");
        }
    }

    public void validateProfilePicture(String profilePicture) throws FailedToParseTheBodyException {
        if (profilePicture == null) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Profile picture is missing");
        }
//        if (!Base64.isBase64(profilePicture)) {
//            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
//                    Response.Status.BAD_REQUEST, "Profile picture has a wrong format");
//        }
    }
}
