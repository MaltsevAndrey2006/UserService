package andrey.dev.userservice.service;

import andrey.dev.userservice.entity.PaymentCard;
import andrey.dev.userservice.entity.User;
import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.exception.exceptions.PaymentCardCreatingException;
import andrey.dev.userservice.exception.exceptions.PaymentCardNotFoundException;
import andrey.dev.userservice.exception.exceptions.PaymentCardsCountException;
import andrey.dev.userservice.exception.exceptions.UserNotFoundException;
import andrey.dev.userservice.mapper.PaymentCardRequestMapper;
import andrey.dev.userservice.mapper.PaymentCardResponseMapper;
import andrey.dev.userservice.repository.PaymentCardRepository;
import andrey.dev.userservice.repository.UserRepository;
import andrey.dev.userservice.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final PaymentCardResponseMapper paymentCardResponseMapper;
    private final PaymentCardRequestMapper paymentCardRequestMapper;
    private final UserRepository userRepository;
    private final UserUtils userUtils;

    @CachePut(value = "paymentCard", key = "#result.id")
    public PaymentCardResponse savePaymentCard(PaymentCardRequest paymentCardRequest) {
        User user = userRepository.findById(paymentCardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + paymentCardRequest.getUserId()));

        Long cardCount = paymentCardRepository.countByUserId(user.getId());
        if (cardCount >= 5) {
            throw new PaymentCardsCountException("User cannot have more than 5 cards");
        }

        return Optional.of(paymentCardRequest)
                .map(request -> paymentCardRequestMapper.toPaymentCard(request, user))
                .map(paymentCardRepository::save)
                .map(paymentCardResponseMapper::toPaymentCardResponse)
                .orElseThrow(() -> new PaymentCardCreatingException("Failed to create payment card"));
    }

    public Page<PaymentCardResponse> getAllPaymentCards(Pageable pageable) {
        return paymentCardRepository.findAll(pageable).map(paymentCardResponseMapper::toPaymentCardResponse);
    }

    @Cacheable(value = "paymentCard", key = "#id")
    public PaymentCardResponse getPaymentCardById(Long id) {
        return paymentCardRepository.findById(id)
                .map(paymentCardResponseMapper::toPaymentCardResponse)
                .orElseThrow(PaymentCardNotFoundException::new);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCard", key = "#id")
    })
    public void deletePaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment card not found with id: " + id));

        userUtils.checkAccessToUser(paymentCard.getUser().getId());

        paymentCardRepository.deleteById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCard", key = "#id")
    })
    public void updatePaymentCardById(Long id, PaymentCardRequest paymentCardRequest) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(PaymentCardNotFoundException::new);

        userUtils.checkAccessToUser(paymentCard.getUser().getId());

        paymentCard.setNumber(paymentCardRequest.getNumber());
        paymentCard.setExpirationDate(paymentCardRequest.getExpirationDate());
        paymentCard.setHolder(paymentCardRequest.getHolder());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCard", key = "#id")
    })
    public PaymentCardResponse activatePaymentCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment card not found with id: " + id));

        userUtils.checkAccessToUser(paymentCard.getUser().getId());
        paymentCard.setActive(true);
        return paymentCardResponseMapper.toPaymentCardResponse(paymentCard);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCard", key = "#id")
    })
    public PaymentCardResponse deactivatePaymentCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new PaymentCardNotFoundException("Payment card not found with id: " + id));

        userUtils.checkAccessToUser(paymentCard.getUser().getId());
        paymentCard.setActive(false);
        return paymentCardResponseMapper.toPaymentCardResponse(paymentCard);
    }

    @Cacheable(value = "paymentCards", key = "#userId")
    public ArrayList<PaymentCardResponse> getPaymentCardsByUserId(Long userId) {
        userUtils.checkAccessToUser(userId);

        return new ArrayList<>(paymentCardRepository.findPaymentCardByUserId(userId).stream()
                .map(paymentCardResponseMapper::toPaymentCardResponse)
                .toList());
    }
}