package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberWithdrawResponseDto {

    @NotNull
    private String loginId;

    @NotNull String email;
}
