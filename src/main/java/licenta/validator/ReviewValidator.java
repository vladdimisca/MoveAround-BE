package licenta.validator;

import licenta.exception.ExceptionMessage;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.model.Review;
import licenta.util.enumeration.TravelRole;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@ApplicationScoped
public class ReviewValidator implements Validator<Review> {

    @Override
    public void validate(Review review, ValidationMode validationMode) throws FailedToParseTheBodyException {
        validateText(review.getText());
        validateRating(review.getRating());
        validateTravelRole(review.getTravelRole());
    }

    public void validateText(String text) throws FailedToParseTheBodyException {
        if (text == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Text is missing");
        }
        if (text.length() < 2 || text.length() > 200) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Text must be between 2 and 200 characters long");
        }
    }

    public void validateRating(Integer rating) throws FailedToParseTheBodyException {
        if (rating == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Rating is missing");
        }
        if (Arrays.stream(new int[]{1, 2, 3, 4, 5}).noneMatch(rating::equals)) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "Rating must be an integer number between 1 and 5");
        }
    }

    public void validateTravelRole(String travelRole) throws FailedToParseTheBodyException {
        if (travelRole == null) {
            throw new FailedToParseTheBodyException(
                    ExceptionMessage.FAILED_TO_PARSE_THE_BODY, Response.Status.BAD_REQUEST, "Travel role is missing");
        }
        if (!travelRole.equals(TravelRole.PASSENGER.name()) && !travelRole.equals(TravelRole.DRIVER.name())) {
            throw new FailedToParseTheBodyException(ExceptionMessage.FAILED_TO_PARSE_THE_BODY,
                    Response.Status.BAD_REQUEST, "The provided travel role does not exist");
        }
    }
}
