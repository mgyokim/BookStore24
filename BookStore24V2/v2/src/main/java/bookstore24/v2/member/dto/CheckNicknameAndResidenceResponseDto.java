package bookstore24.v2.member.dto;

import bookstore24.v2.domain.Residence;
import lombok.Data;

@Data
public class CheckNicknameAndResidenceResponseDto {

    private String nickname;    // 회원의 닉네임

    private Residence residence;   // 회원의 거주지
}
