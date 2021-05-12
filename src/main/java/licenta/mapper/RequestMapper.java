package licenta.mapper;

import licenta.dto.RequestDTO;
import licenta.mapper.util.RequestMapperUtil;
import licenta.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = RequestMapperUtil.class)
public interface RequestMapper {
    RequestMapper mapper = Mappers.getMapper(RequestMapper.class);

    RequestDTO fromRequest(Request request);
}
