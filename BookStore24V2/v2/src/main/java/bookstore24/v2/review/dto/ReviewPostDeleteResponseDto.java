package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewPostDeleteResponseDto {

    @NotNull
    private String loginId; // Review 삭제 요청한 회원의 loginId
}
