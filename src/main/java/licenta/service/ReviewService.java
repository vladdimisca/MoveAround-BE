package licenta.service;

import licenta.dao.ReviewDAO;
import licenta.exception.ExceptionMessage;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.exception.definition.ForbiddenActionException;
import licenta.exception.definition.ReviewNotFoundException;
import licenta.exception.definition.UserNotFoundException;
import licenta.model.Review;
import licenta.model.User;
import licenta.util.enumeration.Authentication;
import licenta.validator.ReviewValidator;
import licenta.validator.ValidationMode;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReviewService {

    @Inject
    ReviewDAO reviewDAO;

    @Inject
    UserService userService;

    @Inject
    ReviewValidator reviewValidator;

    @Inject
    JsonWebToken jwt;

    @Transactional
    public Review createReview(Review review) throws UserNotFoundException, FailedToParseTheBodyException {
        reviewValidator.validate(review, ValidationMode.CREATE);

        // TODO: add conditions -
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        review.setSender(userService.getUserById(userId));
        review.setReceiver(userService.getUserById(review.getReceiver().getId()));
        review.setDateTime(LocalDateTime.now());
        review.setId(0);

        reviewDAO.persist(review);
        return review;
    }

    public List<Review> getReviewsByUserId(UUID userId) throws UserNotFoundException {
        User user = userService.getUserById(userId);
        return user.getMessagesReceived();
    }

    public Review getReviewById(Integer reviewId) throws ReviewNotFoundException {
        return reviewDAO.getReviewById(reviewId).orElseThrow(() ->
                new ReviewNotFoundException(ExceptionMessage.REVIEW_NOT_FOUND, Response.Status.NOT_FOUND));
    }

    @Transactional
    public Review updateReviewById(Integer reviewId, Review review)
            throws ReviewNotFoundException, ForbiddenActionException, FailedToParseTheBodyException {

        Review existingReview = getReviewById(reviewId);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (!existingReview.getSender().getId().equals(userId)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You are not allowed to update this review");
        }

        existingReview.setRating(review.getRating());
        existingReview.setText(review.getText());
        existingReview.setTravelRole(review.getTravelRole());

        reviewValidator.validate(existingReview, ValidationMode.UPDATE);

        reviewDAO.persist(existingReview);
        return existingReview;
    }

    @Transactional
    public void deleteReviewById(Integer reviewId) throws ReviewNotFoundException, ForbiddenActionException {
        Review review = getReviewById(reviewId);
        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));

        if (!review.getSender().getId().equals(userId)) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You are not allowed to delete this review");
        }

        reviewDAO.delete(review);
    }

    public double computeAvgRatingByUserId(UUID userId) throws UserNotFoundException {
        User user = userService.getUserById(userId);

        return user.getMessagesReceived().stream()
                .collect(Collectors.averagingDouble(Review::getRating));
    }

}
