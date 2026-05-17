package andrey.dev.userservice.exception.exceptions;

public class UserCreatingException extends RuntimeException {
    public UserCreatingException(String message) {
        super(message);
    }

    public UserCreatingException() {
    }
}
