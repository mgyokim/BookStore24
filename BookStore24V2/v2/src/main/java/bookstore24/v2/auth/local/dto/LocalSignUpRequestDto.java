package bookstore24.v2.auth.local.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LocalSignUpRequestDto {

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String loginPassword1;
    @NotEmpty
    private String loginPassword2;
    @NotEmpty
    private String email;
}
