package bookstore24.v2.auth.local;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LocalSignUpRequestDto {

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String loginPassword;
    @NotEmpty
    private String email;
}
