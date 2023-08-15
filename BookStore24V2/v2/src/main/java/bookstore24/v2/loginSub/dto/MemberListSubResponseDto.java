package bookstore24.v2.loginSub.dto;

import lombok.Data;

@Data
public class MemberListSubResponseDto {

    private Long id;

    private String loginId;

    private String email;

    private String provider;

    private String nickname;

    private String residence;

    private String role;

    private String profileImg;
}
