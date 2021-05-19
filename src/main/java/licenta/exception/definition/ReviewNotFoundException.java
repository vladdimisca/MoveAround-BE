package licenta.exception.definition;

import licenta.exception.ExceptionMessage;

import javax.ws.rs.core.Response;

public class ReviewNotFoundException extends AbstractApiException {
    public ReviewNotFoundException(ExceptionMessage exception, Response.StatusType status, Object... messageArgs) {
        super(exception, status, messageArgs);
    }
}
