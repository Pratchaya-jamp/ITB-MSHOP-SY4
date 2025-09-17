package intregrated.backend.dtos.users;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class UserResponseDto {
    private Integer id;
    private String nickname;
    private String email;
    private String fullname;
    private Boolean isActive;
    private String userType;
}
