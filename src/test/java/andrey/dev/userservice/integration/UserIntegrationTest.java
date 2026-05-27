package andrey.dev.userservice.integration;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.UserRequest;
import andrey.dev.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
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

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Long testUserId;

    private static final String PRIVATE_KEY_PEM = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCnDl1ZrTtwaklNNpYpNrhIwEB4w3vbKiUvqJdTVStwtH9FAv02Blbza2yfAqFIGHpO/pVR2VKKcKFpy2V2+o7Xx9F2eOLsj3PgjddyNncLvDl7YIL5pwPFAr7anxuzlj9h0CnZkmTU0+BhHyLV9rQlK0RXjHEHBKV3J9vLCk5g6iJpBwbgL+f/5rGqnIBpqNLJxBGAIa5I71rX8nGhE3amsY9qd9wUakbFUHkcDuTWEhWZcyRWDK6OlF11WAesA173ZHTMMfA1tpP12AGMVf0ysZi59M4Jp9lYq/gZVSuvVkYTBwCGUz4UE0x6zscFgwdyBsfHyEF6fv4+l52AQRXnAgMBAAECggEAFKhKEtTBDN2XwtyFJQOHNjfPwR8rKabEgmgujjdx77XiJv5/oTaXefJGtEL2/ptIRIxmmoBtHIFg9FwaZ+QD1dr3o9a++NGkWpgvlAf4IJNd6Eu+5nAonyv/vbj/C+4AWHANPMJFhavNizT2cc7X5+C5yrmrIFsKuvlKzIixuHoH3RTpUD6vJzB7Jyzlb02/ZcdPgpwS5DowKoNoh4JubU1b6G3KK0Qm2BvzvvVsbgf/e9s0+OuZotcS4SmVLXHncFSi1YxqSo6b010z4HQ3+bzyRkO/+GpBwHYHy3jABezGwHCxBwrj74OsN2e3oUSa6vewoE5D6OKWhQtN6szgQQKBgQDnxIPkc5Y29C52GGmLy+Kf5MjztdSXY5BHJbWEHHYfX7N/+u6MIMJ4gRvCaN9IE7QJzBmUFckdbZt45rQgzkZ4XzupeTTYXgvlqbjR7L0WSMud77waP7M09SsRfUYem0O5jy9IhEmJhNaPrxV2IQtN8xElbpOHERnvN6OpHSR0hQKBgQC4hcht0rJ6BWnRMGWbBcrJ6A3nJKjnYEhyUNR15EzKRVtGF4m8VUGkbxQ+kYm5daeqIeyeBRxQvF6OX78/WNbGroWxem+T9TS208wRHnUAwU77clbUw1CP1TtlbMivnaZ98gs56WacORLr9MrIpenRxSG+58WnO3K5XlWpZAjSewKBgHXFQBgYPB6Umf9cjFWDNxd01EAzB2IeL7RXjxMgu01Z/gZsZkdCZk+Bm6+ARuWDTZsk4WKEZ3vStIwM/z8kUl7cVZ7afmXr9DOxuL7Dg5oNR5prtbPI5rFkW4w5kiX/U7y465f30L5WiAjfORKb2/iyKOZSeBjMMdeC+GD49AtZAoGBAIvcw8YSnTuGHOX1xB4T7tjJrrgT/n6aaW9UuyW87UOn/H4NW1ZIXSARHgwq7nSHrJV1b097WjIMBbPu+Rw/71PbdvTGdAp3IwStVxFmv5LZ807+JLjSbp8HJiVDpn4OheMS8tVrh15EmIYHHymlMKzSujhkn1mZ4uSEj3N8on8/AoGBAJe+Otph15eFzxQx+32pvpkMzsPmSvW8FfOpbLJ69M1koRO6Sq2TKxXkg68X7IyPB+sJrw7yKe8Hg/SX+w64Y1347MbcOxwQN9hlZE3bVFb97tntLbd/AGx61dHzvNnmjBhhK8db6f0yCzQ/wE19S3rkjCEyJpumH6FvwQ6iG1BJ";

    private PrivateKey getPrivateKey() throws Exception {
        String privateKeyContent = PRIVATE_KEY_PEM;

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private String generateJwtToken(Long userId, String role) throws Exception {
        PrivateKey privateKey = getPrivateKey();
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

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

        registry.add("jwt.public-key", () -> "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApw5dWa07cGpJTTaWKTa4SMBAeMN72yolL6iXU1UrcLR/RQL9NgZW82tsnwKhSBh6Tv6VUdlSinChactldvqO18fRdnji7I9z4I3XcjZ3C7w5e2CC+acDxQK+2p8bs5Y/YdAp2ZJk1NPgYR8i1fa0JStEV4xxBwSldyfbywpOYOoiaQcG4C/n/+axqpyAaajSycQRgCGuSO9a1/JxoRN2prGPanfcFGpGxVB5HA7k1hIVmXMkVgyujpRddVgHrANe92R0zDHwNbaT9dgBjFX9MrGYufTOCafZWKv4GVUrr1ZGEwcAhlM+FBNMes7HBYMHcgbHx8hBen7+PpedgEEV5wIDAQAB");
    }

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        testUserId = 1L;
        jwtToken = generateJwtToken(testUserId, "ADMIN");
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Andreu");
        userRequest.setEmail("andrey@gmail.com");
        userRequest.setSurname("Maltsev");
        userRequest.setBirthDate(LocalDate.of(2006, 12, 11));

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtToken)
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
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
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
                        .header("Authorization", "Bearer " + jwtToken)
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
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUserRequest)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
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
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(patch("/api/v1/users/deactivate/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/v1/users/activate/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
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
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Long userId = createdUser.getId();

        mockMvc.perform(delete("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
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
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtToken)
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