package andrey.dev.userservice.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCardResponse {

    private Long id;

    private Long userId;

    private String number;

    private LocalDateTime expirationDate;

    private String holder;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
