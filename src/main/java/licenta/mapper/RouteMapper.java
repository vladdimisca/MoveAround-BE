package licenta.mapper;

import licenta.dto.RouteDTO;
import licenta.mapper.util.RouteMapperUtil;
import licenta.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = RouteMapperUtil.class)
public interface RouteMapper {
    RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    @Mapping(source = "user", target = "userId", qualifiedBy = RouteMapperUtil.User.class)
    @Mapping(source = "car", target = "licensePlate", qualifiedBy = RouteMapperUtil.Car.class)
    RouteDTO fromRoute(Route route);
}
