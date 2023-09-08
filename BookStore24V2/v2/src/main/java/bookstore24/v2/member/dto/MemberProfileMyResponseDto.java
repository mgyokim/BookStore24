package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberProfileMyResponseDto {

    @NotNull
    private Long id;    // 회원 테이블의 id

    @NotNull
    private String email;   // 회원의 email

    @NotNull
    private String provider;    // 회원이 회원가입한 경로

    @NotNull
    private String nickname;    // 회원의 닉네임

    @NotNull
    private String residence;   // 회원의 거주지

    @NotNull
    private String profileImg;  // 회원의 프로필이미지

}
