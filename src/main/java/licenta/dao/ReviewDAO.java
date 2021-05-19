package licenta.dao;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import licenta.model.Review;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ReviewDAO implements PanacheRepository<Review> {

    public Optional<Review> getReviewById(Integer reviewId) {
        return find("id", reviewId).firstResultOptional();
    }
}
