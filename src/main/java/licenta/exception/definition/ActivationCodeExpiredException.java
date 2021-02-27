package licenta.exception.definition;

import licenta.exception.ExceptionMessage;

import javax.ws.rs.core.Response;

public class ActivationCodeExpiredException extends AbstractApiException {
    public ActivationCodeExpiredException(ExceptionMessage exception, Response.StatusType status, Object... messageArgs) {
        super(exception, status, messageArgs);
    }
}
