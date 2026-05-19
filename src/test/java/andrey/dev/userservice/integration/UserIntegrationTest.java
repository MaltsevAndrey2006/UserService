package andrey.dev.userservice.integration;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import andrey.dev.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    private final static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    private static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Andreu"))
                .andExpect(jsonPath("$.email").value("andrey@gmail.com"))
                .andExpect(jsonPath("$.surname").value("Maltsev"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnUserById() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Andreu"))
                .andExpect(jsonPath("$.email").value("andrey@gmail.com"))
                .andExpect(jsonPath("$.surname").value("Maltsev"))
                .andExpect(jsonPath("$.birthDate").value("2006-12-11"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        UserRequest updatedUserRequest = new UserRequest();
        updatedUserRequest.setName("Dima");
        updatedUserRequest.setEmail("andrey@gmail.com");
        updatedUserRequest.setSurname("Maltsev");
        updatedUserRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        mockMvc.perform(patch("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dima"));
    }

    @Test
    void userShouldBeDeactivatedAndActivated() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(patch("/api/v1/users/deactivate/" + userId))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/v1/users/activate/" + userId))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        UserRequest user1 = new UserRequest();
        user1.setName("Andreu");
        user1.setEmail("andrey@gmail.com");
        user1.setSurname("Maltsev");
        user1.setBirthDate(LocalDate.of(2006, 12, 11));

        UserRequest user2 = new UserRequest();
        user2.setName("Dima");
        user2.setEmail("dima@gmail.com");
        user2.setSurname("Ivanov");
        user2.setBirthDate(LocalDate.of(2005, 5, 15));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }
}