package andrey.dev.userservice.exception.exceptions;

public class UserUpdateException extends RuntimeException {
    public UserUpdateException(String message) {
        super(message);
    }

    public UserUpdateException() {
    }
}
