package licenta.exception;

public enum ExceptionMessage {

    INTERNAL_SERVER_ERROR(1, "Internal server error. Oops, something went wrong!"),
    FAILED_TO_PARSE_THE_BODY(2, "{0}!"),
    EMAIL_ALREADY_EXISTS(3, "This email is already used!"),
    PHONE_NUMBER_ALREADY_EXISTS(4, "This phone number is already used!"),
    USER_NOT_FOUND(5, "This user does not exist!"),
    WRONG_PASSWORD(6, "The provided password is incorrect!"),
    FORBIDDEN_ACTION(7, "{0}!"),
    ACTIVATION_CODE_NOT_FOUND(8, "{0}!"),
    WRONG_ACTIVATION_CODE(9, "This activation code is wrong!"),
    ACTIVATION_CODE_EXPIRED(10, "This activation code has expired!"),
    CAR_NOT_FOUND(11, "This car does not exist!"),
    LICENSE_PLATE_ALREADY_EXISTS(12, "There is another car with this license plate!");

    private final int errorCode;
    private final String errorMessage;

    ExceptionMessage(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
