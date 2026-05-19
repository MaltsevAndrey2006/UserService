package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import andrey.dev.userservice.entity.dto.UserResponse;
import andrey.dev.userservice.exception.exceptions.UserCreatingException;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.exception.exceptions.UserUpdateException;
import andrey.dev.userservice.mapper.UserRequestMapper;
import andrey.dev.userservice.mapper.UserResponseMapper;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.repository.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;

    @CachePut(value = "users", key = "#result.id")
    public UserResponse saveUser(UserRequest userRequest) {
        return Optional.ofNullable(userRequest)
                .map(userRequestMapper::toUser)
                .map(userRepository::save)
                .map(userResponseMapper::toUserResponse).orElseThrow(UserCreatingException::new);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void updateUser(UserRequest userRequest, Long id) {
        if (userRequest == null) {
            throw new IllegalArgumentException("userRequest cannot be null");
        }
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        User user = userRequestMapper.toUser(userRequest);

        int updatedRows = userRepository.updateUser(user, id);

        if (updatedRows <= 0) {
            throw new UserUpdateException("User update failed");
        }
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void activateUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.activateUser(id);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deactivateUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deactivateUser(id);
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userResponseMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException("no user with id :" + id));
    }

    public Page<UserResponse> getAllUsers(String firstName, String surName, Pageable pageable) {
        List<Specification<User>> specs = new ArrayList<>();

        if (firstName != null && !firstName.isBlank()) {
            specs.add(UserSpecifications.filterByFirstName(firstName));
        }

        if (surName != null && !surName.isBlank()) {
            specs.add(UserSpecifications.filterBySurName(surName));
        }

        Specification<User> spec = Specification.allOf(specs);

        return userRepository.findAll(spec, pageable).map(userResponseMapper::toUserResponse);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }


}
