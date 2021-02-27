package licenta.validator;

import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.exception.definition.InternalServerErrorException;

public interface Validator<T> {
    void validate(T genericEntity, ValidationMode validationMode)
            throws FailedToParseTheBodyException, InternalServerErrorException;
}
