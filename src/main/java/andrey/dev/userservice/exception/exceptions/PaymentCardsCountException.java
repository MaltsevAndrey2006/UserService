package andrey.dev.userservice.exception.exceptions;

public class PaymentCardsCountException extends RuntimeException {
    public PaymentCardsCountException() {}
    public PaymentCardsCountException(String message) {
        super(message);
    }
}
