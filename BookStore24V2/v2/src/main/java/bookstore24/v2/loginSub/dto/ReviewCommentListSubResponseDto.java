package bookstore24.v2.loginSub.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ReviewCommentListSubResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private String content;

    @NotNull
    private Long memberId;

    @NotNull
    private Long reviewId;


}
