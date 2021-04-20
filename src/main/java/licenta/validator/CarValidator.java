package licenta.validator;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.model.Car;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.time.Year;
import java.util.Arrays;

@ApplicationScoped
public class CarValidator implements Validator<Car> {
    private static final String[] COLORS =
        {"black", "white", "grey", "red", "blue", "green", "brown", "beige", "orange", "yellow", "pink", "darkblue"};

    @Override
    public void validate(Car car, ValidationMode validationMode) throws FailedToParseTheBodyException {
        validateLicensePlate(car.getLicensePlate());
        validateMake(car.getMake());
        validateModel(car.getModel());
        validateColor(car.getColor());
        validateYear(car.getYear());
    }

    public void validateLicensePlate(String licensePlate) throws FailedToParseTheBodyException {
        if (licensePlate == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "License plate is missing");
        }
        if (licensePlate.length() < 4) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "License plate should be at least 4 characters long");
        }
    }

    public void validateMake(String make) throws FailedToParseTheBodyException {
        if (make == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Make is missing");
        }
        if (make.length() < 1) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Make should be at least one character long");
        }
    }

    public void validateModel(String model) throws FailedToParseTheBodyException {
        if (model == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Model is missing");
        }
        if (model.length() < 1) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Model should be at least one character long");
        }
    }

    public void validateColor(String color) throws FailedToParseTheBodyException {
        if (color == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Color is missing");
        }
        if (Arrays.stream(COLORS).noneMatch(color::equals)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "This color is not recognized");
        }
    }

    public void validateYear(Integer year) throws FailedToParseTheBodyException {
        if (year == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Year is missing");
        }

        int currentYear = Year.now().getValue();
        if (year < currentYear - 25 || year > currentYear) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST,
                    "Year must be at least " + (currentYear - 25) + " and at most " + currentYear);
        }
    }
}
