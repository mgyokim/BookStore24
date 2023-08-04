package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PasswordEditSaveRequestDto {

    @NotNull
    private String nowPassword;

    @NotNull
    private String password1;

    @NotNull
    private String password2;
}
