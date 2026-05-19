package andrey.dev.userservice.exception.exceptions;

public class PaymentCardCreatingException extends RuntimeException {
    public PaymentCardCreatingException(String message) {
        super(message);
    }

    public PaymentCardCreatingException() {
    }
}
