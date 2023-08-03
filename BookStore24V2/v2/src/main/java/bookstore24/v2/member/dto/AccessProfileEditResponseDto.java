package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AccessProfileEditResponseDto {

    @NotNull
    private String loginId;

    @NotNull
    private String email;

    @NotNull
    private String nickname;

    @NotNull
    private String residence;

    private String profileImg;
}
