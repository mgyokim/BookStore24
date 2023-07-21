package bookstore24.v2.auth;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LocalSignUpDto {

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String loginPassword;
    @NotEmpty
    private String email;
}