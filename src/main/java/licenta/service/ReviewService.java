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
import licenta.util.enumeration.TravelRole;
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
    public Review createReview(Review review)
            throws UserNotFoundException, FailedToParseTheBodyException, ForbiddenActionException {

        reviewValidator.validate(review, ValidationMode.CREATE);

        UUID userId = UUID.fromString(jwt.getClaim(Authentication.ID_CLAIM.getValue()));
        review.setSender(userService.getUserById(userId));
        review.setReceiver(userService.getUserById(review.getReceiver().getId()));

        LocalDateTime currentDateTime = LocalDateTime.now();
        boolean check;
        if (review.getTravelRole().equals(TravelRole.PASSENGER)) {
            // must have at least one waypoint associated with one of the receiver's routes which has a past start date
            check = review.getSender().getRoutes().stream()
                    .filter(route -> route.getParentRoute() != null)
                    .filter(subRoute -> subRoute.getParentRoute().getStartDate().isBefore(currentDateTime))
                    .anyMatch(subRoute ->
                            subRoute.getParentRoute().getUser().getId().equals(review.getReceiver().getId()));
        } else {
            // must have at least one route which has a past start date and has an associated waypoint from the receiver
            check = review.getReceiver().getRoutes().stream()
                    .filter(route -> route.getParentRoute() != null)
                    .filter(subRoute -> subRoute.getParentRoute().getStartDate().isBefore(currentDateTime))
                    .anyMatch(subRoute ->
                            subRoute.getParentRoute().getUser().getId().equals(review.getSender().getId()));
        }
        if (!check) {
            throw new ForbiddenActionException(ExceptionMessage.FORBIDDEN_ACTION,
                    Response.Status.FORBIDDEN, "You are not allowed to review this user");
        }

        review.setDateTime(LocalDateTime.now());
        review.setId(0);

        reviewDAO.persist(review);
        return review;
    }

    public List<Review> getReviewsByUserId(UUID userId) throws UserNotFoundException {
        User user = userService.getUserById(userId);
        return user.getReviewsReceived().stream()
                .sorted((review1, review2) -> {
                    if (review1.getDateTime().equals(review2.getDateTime())) {
                        return 0;
                    }
                    return review1.getDateTime().isBefore(review2.getDateTime()) ? 1 : -1;
                })
                .collect(Collectors.toList());
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

        return user.getReviewsReceived().stream()
                .collect(Collectors.averagingDouble(Review::getRating));
    }

}
