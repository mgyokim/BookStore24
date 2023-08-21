package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ReviewPostDetailReviewCommentResponseDto {

    @NotNull
    private Long reviewCommentId;    // 댓글 id

    @NotNull
    private String content;     // 댓글 본문

    @NotNull
    private LocalDateTime createdDate;  // 댓글 작성 날짜

    @NotNull
    private String nickname;     // 댓글 작성자 닉네임

    @NotNull
    private String loginId;    // 댓글 작성자 로그인 아이디

    @NotNull
    private Long reviewId;    // 리뷰 id



}
