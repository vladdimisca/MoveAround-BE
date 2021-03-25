package licenta.mapper;

import licenta.dto.CarDTO;
import licenta.mapper.util.CarMapperUtil;
import licenta.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CarMapperUtil.class)
public interface CarMapper {
    CarMapper mapper = Mappers.getMapper(CarMapper.class);

    @Mapping(source = "user", target = "userId", qualifiedBy = CarMapperUtil.User.class)
    CarDTO fromCar(Car car);
}
