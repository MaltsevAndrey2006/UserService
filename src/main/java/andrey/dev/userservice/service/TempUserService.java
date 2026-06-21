package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.dto.TempUserRequest;
import andrey.dev.userservice.entity.dto.TempUserResponse;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.mapper.TempUserRequestMapper;
import andrey.dev.userservice.mapper.TempUserResponseMapper;
import andrey.dev.userservice.repository.TempUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempUserService {

    private final TempUserRepository tempUserRepository;
    private final TempUserRequestMapper tempUserRequestMapper;
    private final TempUserResponseMapper tempUserResponseMapper;

    public TempUserResponse save(TempUserRequest tempUser) {
        return tempUserResponseMapper.toResponse(tempUserRepository.save(tempUserRequestMapper.toUser(tempUser)));
    }

    public TempUserResponse findById(Long id) {
        return tempUserRepository.findById(id).map(tempUserResponseMapper::toResponse).orElseThrow(() -> new UserNotFoundException("no user with id:" + id));
    }

    @Transactional
    public void deleteByEmail(String email) {
        tempUserRepository.deleteByEmail(email);
    }

    public TempUserResponse findByEmail(String email) {
        return tempUserRepository.findByEmail(email).map(tempUserResponseMapper::toResponse).orElseThrow(() -> new UserNotFoundException("no user with email:" + email));

    }
}
