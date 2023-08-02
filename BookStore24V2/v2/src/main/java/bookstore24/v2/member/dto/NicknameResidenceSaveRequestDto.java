package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NicknameResidenceSaveRequestDto {

    @NotNull
    private String nickname;

    @NotNull
    private String residence;

}
