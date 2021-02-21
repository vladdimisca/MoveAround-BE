package licenta.exception.definition;

import licenta.exception.ExceptionMessage;

import javax.ws.rs.core.Response;
import java.text.MessageFormat;

public abstract class AbstractApiException extends Exception {
    private final int errorCode;
    private final Response.StatusType httpStatus;

    protected AbstractApiException(ExceptionMessage exception, Response.StatusType httpStatus, Object... messageArgs) {
        super(formatMessage(exception.getErrorMessage(), messageArgs));
        this.httpStatus = httpStatus;
        this.errorCode = exception.getErrorCode();
    }

    private static String formatMessage(String message, Object... messageArgs) {
        return MessageFormat.format(message, messageArgs);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Response.StatusType getHttpStatus() {
        return httpStatus;
    }
}
