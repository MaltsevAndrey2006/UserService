package andrey.dev.userservice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
