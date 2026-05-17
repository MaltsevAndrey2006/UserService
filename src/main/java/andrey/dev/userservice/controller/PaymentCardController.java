package andrey.dev.userservice.controller;

import andrey.dev.userservice.entity.dto.PaymentCardRequest;
import andrey.dev.userservice.entity.dto.PaymentCardResponse;
import andrey.dev.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payment-cards")
public class PaymentCardController {
    private final PaymentCardService paymentCardService;

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponse>> getPaymentCards(Pageable pageable) {
        return ResponseEntity.ok(paymentCardService.getAllPaymentCards(pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<PaymentCardResponse> getPaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentCardResponse> createPaymentCard(@RequestBody @Valid PaymentCardRequest paymentCardRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.savePaymentCard(paymentCardRequest));
    }

    @PatchMapping("{id}")
    public ResponseEntity<Void> updatePaymentCardById(@RequestBody @Valid PaymentCardRequest paymentCardRequest
            , @PathVariable Long id) {
        paymentCardService.updatePaymentCardById(id, paymentCardRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("activate/{id}")
    public ResponseEntity<Void> activatePaymentCard(@PathVariable Long id) {
        paymentCardService.activatePaymentCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("deactivate/{id}")
    public ResponseEntity<Void> deactivatePaymentCard(@PathVariable Long id) {
        paymentCardService.deactivatePaymentCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("user/{user-id}")
    public ResponseEntity<List<PaymentCardResponse>> getPaymentCardByUserId(@PathVariable(name = "user-id") Long userId) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(userId));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePaymentCardById(@PathVariable Long id) {
        paymentCardService.deletePaymentCardById(id);
        return ResponseEntity.noContent().build();
    }
}
