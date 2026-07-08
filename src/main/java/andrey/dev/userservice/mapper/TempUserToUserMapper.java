package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.TempUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TempUserToUserMapper {

    @Mapping(target = "id", ignore = true)
    User toUser(TempUserResponse tempUser);
}
