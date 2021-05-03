package licenta.validator;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.model.Route;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

@ApplicationScoped
public class RouteValidator implements Validator<Route> {

    @Override
    public void validate(Route route, ValidationMode validationMode) throws FailedToParseTheBodyException {
        validateStartDate(route.getStartDate());
        validatePriceByDistance(route.getPrice());
        validateAvailableSeats(route.getAvailableSeats());
        validateCoordinates(
                route.getStartLatitude(), route.getStartLongitude(), route.getStopLatitude(), route.getStopLongitude());
    }

    public void validateStartDate(LocalDateTime startDate) throws FailedToParseTheBodyException {
        if (startDate == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Start date is missing");
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        if (startDate.isBefore(currentDateTime)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Start date cannot be a past date");
        }
    }

    public void validatePriceByDistance(Double price) throws FailedToParseTheBodyException {
        if (price == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Price is missing");
        }
        // TODO: validate
    }

    public void validateAvailableSeats(Integer availableSeats) throws FailedToParseTheBodyException {
        if (availableSeats == null) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "The number of available seats is missing");
        }
        if (availableSeats < 1) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "The number of seats should be greater than 0");
        }
    }

    public void validateCoordinates(Double startLat, Double startLong, Double stopLat, Double stopLong)
            throws FailedToParseTheBodyException {

        validateLatitude(startLat);
        validateLongitude(startLong);
        validateLatitude(stopLat);
        validateLongitude(stopLong);

        if (startLat.equals(stopLat) && startLong.equals(stopLong)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Start and stop are the same");
        }
    }

    public void validateLatitude(Double latitude) throws FailedToParseTheBodyException {
        if (latitude == null) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Latitude cannot be null");
        }
        if (latitude.floatValue() < -90.0 || latitude.floatValue() > 90.0) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Latitude values should be between -90 and 90");
        }
    }

    public void validateLongitude(Double longitude) throws FailedToParseTheBodyException {
        if (longitude == null) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Longitude cannot be null");
        }

        if (longitude.floatValue() < -180.0 || longitude.floatValue() > 180.0) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Longitude values should be between -180 and 180");
        }
    }
}
