package licenta.exception;

public enum ExceptionMessage {

    INTERNAL_SERVER_ERROR(1, "Internal server error. Oops, something went wrong!"),
    FAILED_TO_PARSE_THE_BODY(2, "Failed to parse the request body. {0}!"),
    EMAIL_ALREADY_EXISTS(3, "This email is already used!"),
    PHONE_NUMBER_ALREADY_EXISTS(4, "This phone number is already used!"),
    ROLE_NOT_FOUND(5, "This role does not exist!"),
    USER_NOT_FOUND(6, "This user does not exist!"),
    WRONG_PASSWORD(7, "The provided password is incorrect!"),
    FORBIDDEN_ACTION(8, "{0}!"),
    ACTIVATION_CODE_NOT_FOUND(9, "This activation code does not exist!"),
    WRONG_ACTIVATION_CODE(10, "This activation code is wrong!");

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
