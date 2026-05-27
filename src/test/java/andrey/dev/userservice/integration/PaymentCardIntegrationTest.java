package andrey.dev.userservice.integration;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.service.PaymentCardService;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PaymentCardIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    private User testUser;
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

    @BeforeEach
    void setUp() throws Exception {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Andrey");
        testUser.setEmail("andrey@gmail.com");
        testUser.setBirthDate(LocalDate.of(2006, 12, 11));
        testUser.setSurname("Maltsev");
        testUser = userRepository.save(testUser);

        testUserId = testUser.getId();
        jwtToken = generateJwtToken(testUserId, "ADMIN");
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(testUser.getId());
        request.setNumber("1212312122412412");
        request.setHolder("Andrey");
        request.setExpirationDate(LocalDateTime.now().plusYears(2));

        mockMvc.perform(post("/api/v1/payment-cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("1212312122412412"))
                .andExpect(jsonPath("$.holder").value("Andrey"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    void shouldReturnPaymentCardById() throws Exception {
        PaymentCard card = new PaymentCard();
        card.setUser(testUser);
        card.setNumber("12123123123");
        card.setHolder("TEST USER");
        card.setExpirationDate(LocalDateTime.now().plusYears(2));
        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);

        mockMvc.perform(get("/api/v1/payment-cards/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.number").value("12123123123"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.holder").value("TEST USER"));
    }

    @Test
    void shouldReturn404WhenPaymentCardNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/payment-cards/{id}", 9999L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePaymentCard() throws Exception {
        PaymentCard card = new PaymentCard();
        card.setUser(testUser);
        card.setNumber("12123123123");
        card.setHolder("TEST USER");
        card.setExpirationDate(LocalDateTime.now().plusYears(2));
        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);

        mockMvc.perform(delete("/api/v1/payment-cards/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/payment-cards/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllPaymentCardsPaginated() throws Exception {
        for (int i = 0; i < 15; i++) {
            PaymentCard card = new PaymentCard();
            card.setUser(testUser);
            card.setNumber("12123123123" + i);
            card.setHolder("TEST USER " + i);
            card.setExpirationDate(LocalDateTime.now().plusYears(2));
            card.setActive(true);
            paymentCardRepository.save(card);
        }

        mockMvc.perform(get("/api/v1/payment-cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15));
    }

    @Test
    void shouldReturnCardsByUserId() throws Exception {
        PaymentCard card1 = new PaymentCard();
        card1.setUser(testUser);
        card1.setNumber("12123123123");
        card1.setHolder("TEST USER");
        card1.setExpirationDate(LocalDateTime.now().plusYears(2));
        card1.setActive(true);
        paymentCardRepository.save(card1);

        PaymentCard card2 = new PaymentCard();
        card2.setUser(testUser);
        card2.setNumber("1212312312356");
        card2.setHolder("TEST USER");
        card2.setExpirationDate(LocalDateTime.now().plusYears(2));
        card2.setActive(true);
        paymentCardRepository.save(card2);

        mockMvc.perform(get("/api/v1/payment-cards/user/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].number").value("12123123123"))
                .andExpect(jsonPath("$[1].number").value("1212312312356"));
    }

    @Test
    void shouldActivatePaymentCard() throws Exception {
        PaymentCard card = new PaymentCard();
        card.setUser(testUser);
        card.setNumber("1212312312356");
        card.setHolder("TEST USER");
        card.setExpirationDate(LocalDateTime.now().plusYears(2));
        card.setActive(false);
        PaymentCard savedCard = paymentCardRepository.save(card);

        mockMvc.perform(patch("/api/v1/payment-cards/activate/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is2xxSuccessful());

        PaymentCard updatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updatedCard.isActive()).isTrue();
    }

    @Test
    void shouldDeactivatePaymentCard() throws Exception {
        PaymentCard card = new PaymentCard();
        card.setUser(testUser);
        card.setNumber("1212312312356");
        card.setHolder("TEST USER");
        card.setExpirationDate(LocalDateTime.now().plusYears(2));
        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);

        mockMvc.perform(patch("/api/v1/payment-cards/deactivate/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is2xxSuccessful());

        PaymentCard updatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updatedCard.isActive()).isFalse();
    }

    @Test
    void shouldUpdatePaymentCard() throws Exception {
        PaymentCard card = new PaymentCard();
        card.setUser(testUser);
        card.setNumber("OLD_NUMBER12345");
        card.setHolder("OLD HOLDER");
        card.setExpirationDate(LocalDateTime.now().plusYears(1));
        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);

        PaymentCardRequest updateRequest = new PaymentCardRequest();
        updateRequest.setUserId(testUser.getId());
        updateRequest.setNumber("NEW_NUMBER12345");
        updateRequest.setHolder("NEW HOLDER");
        updateRequest.setExpirationDate(LocalDateTime.now().plusYears(3));

        mockMvc.perform(patch("/api/v1/payment-cards/{id}", savedCard.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is2xxSuccessful());

        PaymentCard updatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updatedCard.getNumber()).isEqualTo("NEW_NUMBER12345");
        assertThat(updatedCard.getHolder()).isEqualTo("NEW HOLDER");
    }

    @Test
    void shouldCreateCardWithTimestamps() throws Exception {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(testUser.getId());
        request.setNumber("1234567891123456");
        request.setHolder("TEST USER");
        request.setExpirationDate(LocalDateTime.now().plusYears(2));

        mockMvc.perform(post("/api/v1/payment-cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
}