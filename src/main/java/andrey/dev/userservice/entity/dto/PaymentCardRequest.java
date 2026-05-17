package andrey.dev.userservice.entity.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCardRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 14, max = 20)
    private String number;

    @NotNull
    private LocalDateTime expirationDate;

    @NotBlank
    private String holder;

}
