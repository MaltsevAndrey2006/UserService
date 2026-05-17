package andrey.dev.userservice.repository;

import andrey.dev.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    Optional<PaymentCard> findById(Long id);

    @Query(value = "SELECT * FROM payment_cards  WHERE user_id = :userId", nativeQuery = true)
    List<PaymentCard> findPaymentCardByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PaymentCard  p SET p.active = :#{#paymentCard.active} , " +
            "p.expirationDate = :#{#paymentCard.expirationDate} , " +
            "p.holder = :#{#paymentCard.holder} , " +
            "p.number = :#{#paymentCard.number} " +
            "WHERE p.id = :id")
    int updatePaymentCard(@Param("paymentCard") PaymentCard paymentCard, @Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentCard  p SET p.active =true WHERE p.id = :id")
    int activatePaymentCard(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentCard  p SET p.active =false WHERE p.id = :id")
    int deactivatePaymentCard(@Param("id") Long id);

    Long countByUserId(Long userId);
}
