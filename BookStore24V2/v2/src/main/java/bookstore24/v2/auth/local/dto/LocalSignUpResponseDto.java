package bookstore24.v2.auth.local.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LocalSignUpResponseDto {

    @NotEmpty
    private String message;
    @NotEmpty
    private String loginId;
    @NotEmpty
    private String email;
}
