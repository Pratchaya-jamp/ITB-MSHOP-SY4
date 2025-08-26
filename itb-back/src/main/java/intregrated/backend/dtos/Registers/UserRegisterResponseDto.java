package intregrated.backend.dtos.Registers;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterResponseDto {
    private Integer id;
    private String nickname;
    private String email;
    private String fullname;
    private String mobile;
    private Boolean isActive;
    private String userType;
}
