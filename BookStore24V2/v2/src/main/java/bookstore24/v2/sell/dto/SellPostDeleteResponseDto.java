package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostDeleteResponseDto {

    @NotNull
    private String loginId; // Sell 삭제 요청한 회원의 loginId

}
