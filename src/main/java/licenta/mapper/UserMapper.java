package licenta.mapper;

import licenta.dto.UserDTO;
import licenta.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserMapper {
    UserDTO fromUser(User user);
}
