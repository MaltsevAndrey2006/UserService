package andrey.dev.userservice.exception.exceptions;

public class PaymentCardUpdateException extends RuntimeException {
    public PaymentCardUpdateException(String message) {
        super(message);
    }
    public PaymentCardUpdateException() {}
}
