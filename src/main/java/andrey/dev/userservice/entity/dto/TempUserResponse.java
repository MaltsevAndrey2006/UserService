package andrey.dev.userservice.entity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TempUserResponse {

    private Long id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private String email;
}
