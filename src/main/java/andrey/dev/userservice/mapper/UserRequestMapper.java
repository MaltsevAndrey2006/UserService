package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserRequestMapper {
    @Mapping(target = "active", constant = "true")
    User toUser(UserRequest userRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromRequest(UserRequest userRequest, @MappingTarget User user);
}
