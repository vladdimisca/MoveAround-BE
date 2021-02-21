package licenta.exception.mapper;

import licenta.exception.definition.AbstractApiException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public abstract class AbstractApiExceptionMapper<E extends AbstractApiException> implements ExceptionMapper<E> {
    @Override
    public abstract Response toResponse(E exception);
}
