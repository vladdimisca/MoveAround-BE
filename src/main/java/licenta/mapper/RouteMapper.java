package licenta.mapper;

import licenta.dto.RouteDTO;
import licenta.mapper.util.RouteMapperUtil;
import licenta.model.Route;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = RouteMapperUtil.class)
public interface RouteMapper {
    RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

    RouteDTO fromRoute(Route route);
}
