package licenta.exception.mapper;

import licenta.exception.ExceptionResponseEntity;
import licenta.exception.definition.AbstractApiException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper extends AbstractApiExceptionMapper<AbstractApiException> {
    @Override
    public Response toResponse(AbstractApiException exception) {
        return Response
                .status(exception.getHttpStatus())
                .entity(new ExceptionResponseEntity(exception.getErrorCode(), exception.getMessage()))
                .build();
    }
}
