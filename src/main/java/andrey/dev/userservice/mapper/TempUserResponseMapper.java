package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.TempUser;
import andrey.dev.userservice.entity.dto.TempUserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TempUserResponseMapper {
    TempUserResponse toResponse(TempUser tempUser);
}
