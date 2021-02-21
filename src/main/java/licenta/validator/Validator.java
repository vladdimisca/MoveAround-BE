package licenta.validator;

import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.exception.definition.InternalServerErrorException;
import licenta.exception.definition.RoleNotFoundException;

public interface Validator<T> {
    void validate(T genericEntity, ValidationMode validationMode) throws FailedToParseTheBodyException, RoleNotFoundException, InternalServerErrorException;
}
