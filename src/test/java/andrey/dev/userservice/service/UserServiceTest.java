package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import andrey.dev.userservice.entity.dto.UserResponse;
import andrey.dev.userservice.exception.exceptions.UserCreatingException;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.mapper.UserRequestMapper;
import andrey.dev.userservice.mapper.UserResponseMapper;
import andrey.dev.userservice.repository.UserRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRequestMapper userRequestMapper;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenExists() {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("andrey");
        user.setActive(true);
        user.setEmail("andrey@gmail.com");
        user.setSurname("Maltsev");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName("andrey");
        userResponse.setActive(true);
        userResponse.setEmail("andrey@gmail.com");
        userResponse.setSurname("Maltsev");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userResponseMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("andrey");
        assertThat(result.isActive()).isEqualTo(true);
        assertThat(result.getEmail()).isEqualTo("andrey@gmail.com");

        verify(userRepository).findById(userId);
        verify(userResponseMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDontExists() {
        Long id = 800L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));

        verify(userRepository).findById(id);
        verify(userResponseMapper, never()).toUserResponse(any());
    }

    @Test
    void shouldReturnUserAfterSave() {
        Long userId = 1L;

        UserRequest userRequest = new UserRequest();
        userRequest.setName("andrey");
        userRequest.setSurname("Maltsev");
        userRequest.setEmail("andrey@gmail.com");

        User user = new User();
        user.setId(userId);
        user.setName("andrey");
        user.setActive(true);
        user.setEmail("andrey@gmail.com");
        user.setSurname("Maltsev");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setName("andrey");
        userResponse.setActive(true);
        userResponse.setEmail("andrey@gmail.com");
        userResponse.setSurname("Maltsev");

        when(userRequestMapper.toUser(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userResponseMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.saveUser(userRequest);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("andrey");
        assertThat(result.isActive()).isEqualTo(true);
        assertThat(result.getEmail()).isEqualTo("andrey@gmail.com");

        verify(userRepository).save(user);
        verify(userRequestMapper).toUser(userRequest);
        verify(userResponseMapper).toUserResponse(user);
    }

    @Test
    void shouldReturnUserCreatingException() {
        assertThrows(UserCreatingException.class, () -> userService.saveUser(null));

        verify(userRequestMapper, never()).toUser(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldUpdateWhenUserExists() {
        Long userId = 1L;
        UserRequest request = new UserRequest();
        request.setName("Updated Name");

        User userToUpdate = new User();
        userToUpdate.setName("Updated Name");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRequestMapper.toUser(request)).thenReturn(userToUpdate);
        when(userRepository.updateUser(userToUpdate, userId)).thenReturn(1);

        userService.updateUser(request, userId);

        verify(userRepository).existsById(userId);
        verify(userRequestMapper).toUser(request);
        verify(userRepository).updateUser(userToUpdate, userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundAfterUpdate() {
        Long userId = 999L;
        UserRequest request = new UserRequest();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(request, userId));

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).updateUser(any(), any());
    }

    @Test
    void shouldDeleteWhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowExceptionAfterDelete() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));


        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void shouldActivateWhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.activateUser(userId)).thenReturn(1);

        userService.activateUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).activateUser(userId);
    }


    @Test
    void shouldDeactivateWhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.deactivateUser(userId)).thenReturn(1);

        userService.deactivateUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deactivateUser(userId);
    }

    @Test
    void shouldReturnAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(1L);
        user1.setName("Andrey");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Dima");

        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        UserResponse response1 = new UserResponse();
        response1.setId(1L);
        UserResponse response2 = new UserResponse();
        response2.setId(2L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userResponseMapper.toUserResponse(user1)).thenReturn(response1);
        when(userResponseMapper.toUserResponse(user2)).thenReturn(response2);

        Page<UserResponse> result = userService.getAllUsers(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userResponseMapper, times(2)).toUserResponse(any(User.class));
    }
}
