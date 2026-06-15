package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import andrey.dev.userservice.entity.dto.UserResponse;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.exception.exceptions.UserUpdateException;
import andrey.dev.userservice.mapper.UserRequestMapper;
import andrey.dev.userservice.mapper.UserResponseMapper;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserRequestMapper userRequestMapper;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("John");
        user.setSurname("Doe");

        userRequest = new UserRequest();
        userRequest.setEmail("updated@example.com");
        userRequest.setName("Jane");
        userRequest.setSurname("Smith");
    }

    @Test
    void shouldUpdateWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userUtils).checkAccessToUser(1L);
        doNothing().when(userRequestMapper).updateUserFromRequest(userRequest, user);

        userService.updateUser(userRequest, 1L);

        verify(userRepository).findById(1L);
        verify(userUtils).checkAccessToUser(1L);
        verify(userRequestMapper).updateUserFromRequest(userRequest, user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundAfterUpdate() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(userUtils).checkAccessToUser(999L);

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userRequest, 999L);
        });

        verify(userRepository).findById(999L);
        verify(userUtils).checkAccessToUser(999L);
        verify(userRequestMapper, never()).updateUserFromRequest(any(), any());
    }

    @Test
    void shouldThrowUserUpdateExceptionWhenNoRowsAffected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userUtils).checkAccessToUser(1L);
        doThrow(new UserUpdateException("Failed to update user")).when(userRequestMapper).updateUserFromRequest(userRequest, user);

        assertThrows(UserUpdateException.class, () -> {
            userService.updateUser(userRequest, 1L);
        });
    }

    @Test
    void shouldActivateUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.activateUser(1L)).thenReturn(1);

        userService.activateUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).activateUser(1L);
    }

    @Test
    void shouldThrowExceptionWhenActivatingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.activateUser(999L);
        });

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).activateUser(any());
    }

    @Test
    void shouldDeactivateUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.deactivateUser(1L)).thenReturn(1);

        userService.deactivateUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deactivateUser(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.deactivateUser(999L);
        });

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deactivateUser(any());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userUtils).checkAccessToUser(1L);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userUtils).checkAccessToUser(1L);
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);
        doNothing().when(userUtils).checkAccessToUser(999L);

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        verify(userUtils).checkAccessToUser(999L);
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userResponseMapper.toUserResponse(user)).thenReturn(new UserResponse());
        doNothing().when(userUtils).checkAccessToUser(1L);

        userService.getUserById(1L);

        verify(userUtils).checkAccessToUser(1L);
        verify(userRepository).findById(1L);
        verify(userResponseMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(userUtils).checkAccessToUser(999L);

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        verify(userUtils).checkAccessToUser(999L);
        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsersShouldFilterByFirstName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userResponseMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        userService.getAllUsers("John", null, pageable);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllUsersShouldFilterBySurname() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userResponseMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        userService.getAllUsers(null, "Doe", pageable);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllUsersShouldFilterByBothFirstNameAndSurname() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userResponseMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        userService.getAllUsers("John", "Doe", pageable);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }
}