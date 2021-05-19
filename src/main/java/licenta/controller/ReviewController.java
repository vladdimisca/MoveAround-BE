package licenta.controller;

import io.quarkus.security.Authenticated;
import licenta.exception.definition.FailedToParseTheBodyException;
import licenta.exception.definition.ForbiddenActionException;
import licenta.exception.definition.ReviewNotFoundException;
import licenta.exception.definition.UserNotFoundException;
import licenta.mapper.ReviewMapper;
import licenta.model.Review;
import licenta.service.ReviewService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewController {

    @Inject
    ReviewService reviewService;

    @POST
    public Response createReview(Review review) throws UserNotFoundException, FailedToParseTheBodyException {
        return Response.ok(ReviewMapper.mapper.fromReview(reviewService.createReview(review))).build();
    }

    @GET
    @Authenticated
    @Path("/{reviewId}")
    public Response getReviewById(@PathParam("reviewId") Integer reviewId) throws ReviewNotFoundException {
        return Response.ok(ReviewMapper.mapper.fromReview(reviewService.getReviewById(reviewId))).build();
    }

    @GET
    @Authenticated
    @Path("/receiver/{userId}")
    public Response getReviewsByUserId(@PathParam("userId") UUID userId) throws UserNotFoundException {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return Response.ok(reviews.stream().map(ReviewMapper.mapper::fromReview).collect(Collectors.toList())).build();
    }

    @PUT
    @Authenticated
    @Path("/{reviewId}")
    public Response updateReview(@PathParam("reviewId") Integer reviewId, Review review)
            throws ForbiddenActionException, FailedToParseTheBodyException, ReviewNotFoundException {

        return Response.ok(ReviewMapper.mapper.fromReview(reviewService.updateReviewById(reviewId, review))).build();
    }

    @DELETE
    @Authenticated
    @Path("/{reviewId}")
    public Response deleteReview(@PathParam("reviewId") Integer reviewId)
            throws ForbiddenActionException, ReviewNotFoundException {

        reviewService.deleteReviewById(reviewId);
        return Response.noContent().build();
    }

    @GET
    @Authenticated
    @Path("/rating/{userId}")
    public Response getAvgRating(@PathParam("userId") UUID userId) throws UserNotFoundException {
        return Response.ok(reviewService.computeAvgRatingByUserId(userId)).build();
    }
}
