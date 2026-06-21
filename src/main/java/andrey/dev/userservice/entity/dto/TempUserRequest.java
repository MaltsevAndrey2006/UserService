package andrey.dev.userservice.entity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TempUserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 2, max = 100)
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @Email
    @NotBlank
    @Size(max = 255)
    private String email;
}
