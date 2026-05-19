package andrey.dev.userservice.exception.exceptions;

public class PaymentCardNotFoundException extends RuntimeException {
    public PaymentCardNotFoundException() {
    }

    public PaymentCardNotFoundException(String message) {
        super(message);
    }
}
