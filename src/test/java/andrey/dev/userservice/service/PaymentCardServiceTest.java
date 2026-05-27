package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.exception.exceptions.PaymentCardNotFoundException;
import andrey.dev.userservice.exception.exceptions.PaymentCardsCountException;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.mapper.PaymentCardRequestMapper;
import andrey.dev.userservice.mapper.PaymentCardResponseMapper;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardRequestMapper paymentCardRequestMapper;

    @Mock
    private PaymentCardResponseMapper paymentCardResponseMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private PaymentCardService paymentCardService;

    @Test
    void shouldReturnPaymentCardWhenExistsByUserId() {
        Long paymentCardId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(paymentCardId);
        paymentCard.setActive(true);
        paymentCard.setUser(user);

        PaymentCardResponse paymentCardResponse = new PaymentCardResponse();
        paymentCardResponse.setId(paymentCardId);
        paymentCardResponse.setActive(true);
        paymentCardResponse.setUserId(userId);

        doNothing().when(userUtils).checkAccessToUser(userId);
        when(paymentCardRepository.findPaymentCardByUserId(userId)).thenReturn(List.of(paymentCard));
        when(paymentCardResponseMapper.toPaymentCardResponse(paymentCard)).thenReturn(paymentCardResponse);

        List<PaymentCardResponse> result = paymentCardService.getPaymentCardsByUserId(userId);

        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getFirst().getUserId()).isEqualTo(userId);
        assertThat(result.getFirst().getId()).isEqualTo(paymentCardId);

        verify(paymentCardRepository).findPaymentCardByUserId(userId);
        verify(paymentCardResponseMapper).toPaymentCardResponse(paymentCard);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCards() {
        Long userId = 999L;

        doNothing().when(userUtils).checkAccessToUser(userId);
        when(paymentCardRepository.findPaymentCardByUserId(userId)).thenReturn(List.of());

        List<PaymentCardResponse> result = paymentCardService.getPaymentCardsByUserId(userId);

        assertThat(result.isEmpty()).isTrue();

        verify(paymentCardRepository).findPaymentCardByUserId(userId);
        verify(paymentCardResponseMapper, never()).toPaymentCardResponse(any());
    }

    @Test
    void shouldReturnPaymentCardWhenExistsById() {
        Long paymentCardId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(paymentCardId);
        paymentCard.setActive(true);
        paymentCard.setUser(user);

        PaymentCardResponse paymentCardResponse = new PaymentCardResponse();
        paymentCardResponse.setId(paymentCardId);
        paymentCardResponse.setActive(true);
        paymentCardResponse.setUserId(userId);

        when(paymentCardRepository.findById(paymentCardId)).thenReturn(Optional.of(paymentCard));
        doNothing().when(userUtils).checkAccessToUser(userId);
        when(paymentCardResponseMapper.toPaymentCardResponse(paymentCard)).thenReturn(paymentCardResponse);

        PaymentCardResponse result = paymentCardService.getPaymentCardById(paymentCardId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentCardId);
        assertThat(result.isActive()).isTrue();

        verify(paymentCardRepository).findById(paymentCardId);
        verify(userUtils).checkAccessToUser(userId);
        verify(paymentCardResponseMapper).toPaymentCardResponse(paymentCard);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenIdWrong() {
        Long paymentCardId = 333L;

        when(paymentCardRepository.findById(paymentCardId))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.getPaymentCardById(paymentCardId)
        );

        verify(paymentCardRepository).findById(paymentCardId);
        verify(paymentCardResponseMapper, never()).toPaymentCardResponse(any());
    }

    @Test
    void shouldSavePaymentCardSuccessfully() {
        Long userId = 1L;

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(userId);

        User user = new User();
        user.setId(userId);

        PaymentCard paymentCard = new PaymentCard();
        PaymentCard savedPaymentCard = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(0L);
        when(paymentCardRequestMapper.toPaymentCard(request, user)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(savedPaymentCard);
        when(paymentCardResponseMapper.toPaymentCardResponse(savedPaymentCard)).thenReturn(response);

        PaymentCardResponse result = paymentCardService.savePaymentCard(request);

        assertThat(result).isEqualTo(response);

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardRequestMapper).toPaymentCard(request, user);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardResponseMapper).toPaymentCardResponse(savedPaymentCard);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenSavingWithInvalidUserId() {
        Long userId = 999L;

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> paymentCardService.savePaymentCard(request)
        );

        verify(userRepository).findById(userId);
        verify(paymentCardRepository, never()).countByUserId(any());
        verify(paymentCardRequestMapper, never()).toPaymentCard(any(), any());
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void shouldThrowPaymentCardsCountExceptionWhenUserHas5Cards() {
        Long userId = 1L;

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(userId);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(5L);

        assertThrows(
                PaymentCardsCountException.class,
                () -> paymentCardService.savePaymentCard(request)
        );

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardRequestMapper, never()).toPaymentCard(any(), any());
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void shouldReturnPageOfPaymentCards() {
        Pageable pageable = PageRequest.of(0, 10);

        PaymentCard paymentCard = new PaymentCard();
        PaymentCardResponse response = new PaymentCardResponse();

        Page<PaymentCard> paymentCardPage = new PageImpl<>(List.of(paymentCard));

        when(paymentCardRepository.findAll(pageable)).thenReturn(paymentCardPage);
        when(paymentCardResponseMapper.toPaymentCardResponse(paymentCard)).thenReturn(response);

        Page<PaymentCardResponse> result = paymentCardService.getAllPaymentCards(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().getFirst()).isEqualTo(response);

        verify(paymentCardRepository).findAll(pageable);
        verify(paymentCardResponseMapper).toPaymentCardResponse(paymentCard);
    }

    @Test
    void shouldDeletePaymentCardSuccessfully() {
        Long paymentCardId = 1L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(true);

        paymentCardService.deletePaymentCardById(paymentCardId);

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository).deleteById(paymentCardId);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenDeletingNonExistentCard() {
        Long paymentCardId = 999L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(false);

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.deletePaymentCardById(paymentCardId)
        );

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository, never()).deleteById(any());
    }

    @Test
    void shouldActivatePaymentCardSuccessfully() {
        Long paymentCardId = 1L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(true);
        when(paymentCardRepository.activatePaymentCard(paymentCardId)).thenReturn(1);

        paymentCardService.activatePaymentCard(paymentCardId);

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository).activatePaymentCard(paymentCardId);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenActivatingNonExistentCard() {
        Long paymentCardId = 999L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(false);

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.activatePaymentCard(paymentCardId)
        );

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository, never()).activatePaymentCard(any());
    }

    @Test
    void shouldDeactivatePaymentCardSuccessfully() {
        Long paymentCardId = 1L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(true);
        when(paymentCardRepository.deactivatePaymentCard(paymentCardId)).thenReturn(1);

        paymentCardService.deactivatePaymentCard(paymentCardId);

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository).deactivatePaymentCard(paymentCardId);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenDeactivatingNonExistentCard() {
        Long paymentCardId = 999L;

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(false);

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.deactivatePaymentCard(paymentCardId)
        );

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(paymentCardRepository, never()).deactivatePaymentCard(any());
    }

    @Test
    void shouldUpdatePaymentCardSuccessfully() {
        Long paymentCardId = 1L;
        Long userId = 1L;

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(userId);

        User user = new User();
        user.setId(userId);

        PaymentCard paymentCard = new PaymentCard();

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRequestMapper.toPaymentCard(request, user)).thenReturn(paymentCard);
        when(paymentCardRepository.updatePaymentCard(paymentCard, paymentCardId)).thenReturn(1);

        paymentCardService.updatePaymentCardById(paymentCardId, request);

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(userRepository).findById(userId);
        verify(paymentCardRequestMapper).toPaymentCard(request, user);
        verify(paymentCardRepository).updatePaymentCard(paymentCard, paymentCardId);
    }

    @Test
    void shouldThrowPaymentCardNotFoundExceptionWhenUpdatingNonExistentCard() {
        Long paymentCardId = 999L;

        PaymentCardRequest request = new PaymentCardRequest();
        request.setUserId(1L);

        when(paymentCardRepository.existsById(paymentCardId)).thenReturn(false);

        assertThrows(
                PaymentCardNotFoundException.class,
                () -> paymentCardService.updatePaymentCardById(paymentCardId, request)
        );

        verify(paymentCardRepository).existsById(paymentCardId);
        verify(userRepository, never()).findById(any());
        verify(paymentCardRequestMapper, never()).toPaymentCard(any(), any());
        verify(paymentCardRepository, never()).updatePaymentCard(any(), any());
    }
}