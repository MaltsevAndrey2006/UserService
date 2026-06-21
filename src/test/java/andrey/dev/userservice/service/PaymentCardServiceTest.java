package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.exception.exceptions.PaymentCardNotFoundException;
import andrey.dev.userservice.mapper.PaymentCardRequestMapper;
import andrey.dev.userservice.mapper.PaymentCardResponseMapper;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardResponseMapper paymentCardResponseMapper;

    @Mock
    private PaymentCardRequestMapper paymentCardRequestMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private User user;
    private PaymentCard paymentCard;
    private PaymentCardRequest paymentCardRequest;
    private PaymentCardResponse paymentCardResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");

        paymentCardRequest = new PaymentCardRequest();
        paymentCardRequest.setUserId(1L);
        paymentCardRequest.setNumber("1234567890123456");
        paymentCardRequest.setHolder("John Doe");

        paymentCardResponse = new PaymentCardResponse();
        paymentCardResponse.setId(1L);
        paymentCardResponse.setUserId(1L);
        paymentCardResponse.setNumber("**** **** **** 3456");
    }

    @Test
    void shouldReturnPaymentCardWhenExistsById() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardResponseMapper.toPaymentCardResponse(paymentCard)).thenReturn(paymentCardResponse);

        PaymentCardResponse result = paymentCardService.getPaymentCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getNumber());
        verify(paymentCardRepository).findById(1L);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenNotFoundById() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> {
            paymentCardService.getPaymentCardById(999L);
        });

        verify(paymentCardRepository).findById(999L);
    }

    @Test
    void shouldDeletePaymentCardSuccessfully() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        doNothing().when(userUtils).checkAccessToUser(1L);
        doNothing().when(paymentCardRepository).deleteById(1L);

        paymentCardService.deletePaymentCardById(1L);

        verify(paymentCardRepository).findById(1L);
        verify(userUtils).checkAccessToUser(1L);
        verify(paymentCardRepository).deleteById(1L);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenDeletingNonExistentCard() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> {
            paymentCardService.deletePaymentCardById(999L);
        });

        verify(paymentCardRepository).findById(999L);
        verify(paymentCardRepository, never()).deleteById(any());
    }


    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenActivatingNonExistentCard() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> {
            paymentCardService.activatePaymentCard(999L);
        });

        verify(paymentCardRepository).findById(999L);
        verify(paymentCardRepository, never()).activatePaymentCard(any());
    }


    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenDeactivatingNonExistentCard() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> {
            paymentCardService.deactivatePaymentCard(999L);
        });

        verify(paymentCardRepository).findById(999L);
        verify(paymentCardRepository, never()).deactivatePaymentCard(any());
    }

    @Test
    void shouldUpdatePaymentCardSuccessfully() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        doNothing().when(userUtils).checkAccessToUser(1L);

        paymentCardService.updatePaymentCardById(1L, paymentCardRequest);

        verify(paymentCardRepository).findById(1L);
        verify(userUtils).checkAccessToUser(1L);
        assertEquals(paymentCardRequest.getNumber(), paymentCard.getNumber());
        assertEquals(paymentCardRequest.getHolder(), paymentCard.getHolder());
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenUpdatingNonExistentCard() {
        when(paymentCardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentCardNotFoundException.class, () -> {
            paymentCardService.updatePaymentCardById(999L, paymentCardRequest);
        });

        verify(paymentCardRepository).findById(999L);
        verify(paymentCardRepository, never()).save(any());
    }


    @Test
    void shouldReturnPaymentCardsByUserId() {
        when(paymentCardRepository.findPaymentCardByUserId(1L)).thenReturn(java.util.List.of(paymentCard));
        when(paymentCardResponseMapper.toPaymentCardResponse(paymentCard)).thenReturn(paymentCardResponse);
        doNothing().when(userUtils).checkAccessToUser(1L);

        var result = paymentCardService.getPaymentCardsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentCardRepository, times(1)).findPaymentCardByUserId(1L);
    }

}