package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.exception.exceptions.*;
import andrey.dev.userservice.mapper.PaymentCardRequestMapper;
import andrey.dev.userservice.mapper.PaymentCardResponseMapper;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardResponseMapper paymentCardResponseMapper;
    private final PaymentCardRequestMapper paymentCardRequestMapper;
    private final UserRepository userRepository;

    public PaymentCardResponse savePaymentCard(PaymentCardRequest paymentCardRequest) {
        User user = userRepository.findById(paymentCardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + paymentCardRequest.getUserId()));

        Long cardCount = paymentCardRepository.countByUserId(user.getId());
        if (cardCount >= 5) {
            throw new PaymentCardsCountException("User cannot have more than 5 cards");
        }

        return Optional.ofNullable(paymentCardRequest)
                .map(request -> paymentCardRequestMapper.toPaymentCard(request, user))
                .map(paymentCardRepository::save)
                .map(paymentCardResponseMapper::toPaymentCardResponse)
                .orElseThrow(() -> new PaymentCardCreatingException("Failed to create payment card"));
    }

    public Page<PaymentCardResponse> getAllPaymentCards(Pageable pageable) {
        return paymentCardRepository.findAll(pageable).map(paymentCardResponseMapper::toPaymentCardResponse);
    }

    public PaymentCardResponse getPaymentCardById(Long id) {
        return paymentCardRepository.findById(id).map(paymentCardResponseMapper::toPaymentCardResponse).orElseThrow(PaymentCardNotFoundException::new);
    }

    @Transactional
    public void deletePaymentCardById(Long id) {
        if (paymentCardRepository.existsById(id)) {
            paymentCardRepository.deleteById(id);
        } else {
            throw new PaymentCardNotFoundException("Payment card not found with id: " + id);
        }
    }

    @Transactional
    public void updatePaymentCardById(Long id, PaymentCardRequest paymentCardRequest) {
        if (paymentCardRepository.existsById(id)) {
            User user = userRepository.findById(paymentCardRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + paymentCardRequest.getUserId()));

            int changes = paymentCardRepository.updatePaymentCard(paymentCardRequestMapper.toPaymentCard(paymentCardRequest, user), id);
            if (changes <= 0) {
                throw new PaymentCardUpdateException("Failed to update payment card");
            }
        } else {
            throw new PaymentCardNotFoundException("Payment card not found with id: " + id);
        }
    }

    @Transactional
    public void activatePaymentCard(Long id) {
        if (paymentCardRepository.existsById(id)) {
            paymentCardRepository.activatePaymentCard(id);
        } else {
            throw new PaymentCardNotFoundException("Payment card not found with id: " + id);
        }
    }

    @Transactional
    public void deactivatePaymentCard(Long id) {
        if (paymentCardRepository.existsById(id)) {
            paymentCardRepository.deactivatePaymentCard(id);
        } else {
            throw new PaymentCardNotFoundException("Payment card not found with id: " + id);
        }
    }

    public List<PaymentCardResponse> getPaymentCardsByUserId(Long userId) {
        return paymentCardRepository.findPaymentCardByUserId(userId).stream()
                .map(paymentCardResponseMapper::toPaymentCardResponse)
                .toList();
    }


}
