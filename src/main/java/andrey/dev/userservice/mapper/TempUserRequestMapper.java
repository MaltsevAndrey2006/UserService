package andrey.dev.userservice.mapper;

import andrey.dev.userservice.entity.TempUser;
import andrey.dev.userservice.entity.dto.TempUserRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TempUserRequestMapper {

    TempUser toUser(TempUserRequest tempUserRequest);
}
