package intregrated.backend.dtos.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class EditUserRequestDto {
    @NotBlank
    @Size(min = 4, max = 40)
    private String fullname;

    @NotBlank(message = "Nickname is required")
    private String nickname;
}
