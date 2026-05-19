package andrey.dev.userservice.integration;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.service.PaymentCardService;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Andrey");
        testUser.setEmail("andrey@gmail.com");
        testUser.setBirthDate(LocalDate.of(2006, 12, 11));
        testUser.setSurname("Maltsev");
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(testUser.getId());
        request.setNumber("1212312122412412");
        request.setHolder("Andrey");
        request.setExpirationDate(LocalDateTime.now().plusYears(2));

        mockMvc.perform(post("/api/v1/payment-cards")
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

        mockMvc.perform(get("/api/v1/payment-cards/{id}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.number").value("12123123123"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.holder").value("TEST USER"));
    }

    @Test
    void shouldReturn404WhenPaymentCardNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/payment-cards/{id}", 9999L))
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

        mockMvc.perform(delete("/api/v1/payment-cards/{id}", savedCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/payment-cards/{id}", savedCard.getId()))
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

        mockMvc.perform(get("/api/v1/payment-cards/user/{userId}", testUser.getId()))
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

        mockMvc.perform(patch("/api/v1/payment-cards/activate/{id}", savedCard.getId()))
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

        mockMvc.perform(patch("/api/v1/payment-cards/deactivate/{id}", savedCard.getId()))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is2xxSuccessful());

        PaymentCard updatedCard = paymentCardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updatedCard.getNumber()).isEqualTo("NEW_NUMBER12345");
        assertThat(updatedCard.getHolder()).isEqualTo("NEW HOLDER");
    }


    @Test
    void shouldCreateCardWithTimestamps() {
        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(testUser.getId());
        request.setNumber("12345678");
        request.setHolder("TEST USER");
        request.setExpirationDate(LocalDateTime.now().plusYears(2));

        PaymentCardResponse response = paymentCardService.savePaymentCard(request);

        PaymentCard savedCard = paymentCardRepository.findById(response.getId()).orElseThrow();
        assertThat(savedCard.getCreatedAt()).isNotNull();
        assertThat(savedCard.getUpdatedAt()).isNotNull();
        assertThat(savedCard.getExpirationDate()).isNotNull();
    }

}