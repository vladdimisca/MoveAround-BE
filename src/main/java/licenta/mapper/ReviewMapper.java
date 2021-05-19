package licenta.mapper;

import licenta.dto.ReviewDTO;
import licenta.mapper.util.ReviewMapperUtil;
import licenta.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ReviewMapperUtil.class)
public interface ReviewMapper {
    ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    ReviewDTO fromReview(Review review);
}
