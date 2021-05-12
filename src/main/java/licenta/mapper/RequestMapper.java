package licenta.mapper;

import licenta.dto.RequestDTO;
import licenta.mapper.util.RequestMapperUtil;
import licenta.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = RequestMapperUtil.class)
public interface RequestMapper {
    RequestMapper mapper = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "user", target = "userId", qualifiedBy = RequestMapperUtil.User.class)
    @Mapping(source = "route", target = "routeId", qualifiedBy = RequestMapperUtil.Route.class)
    RequestDTO fromRequest(Request request);
}
