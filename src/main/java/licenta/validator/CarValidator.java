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
    // TODO: create a map with color: color_value
    private static final String[] COLORS =
            {"Blue", "Black", "White", "Red", "Green", "Brown", "Orange", "Violet", "Grey", "Pink", "Yellow"};

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

    int currentYear = Year.now().getValue();


    public void validateYear(Integer year) throws FailedToParseTheBodyException {
        if (year == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Year is missing");
        }
        if (year < currentYear - 25 || year > currentYear) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST,
                    "Year must be at least " + (currentYear - 25) + " and at most " + currentYear);
        }
    }
}
