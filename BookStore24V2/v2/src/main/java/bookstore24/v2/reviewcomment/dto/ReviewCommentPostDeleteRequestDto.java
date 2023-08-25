package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostDeleteRequestDto {

    @NotNull
    private Long reviewCommentId; // 리뷰 댓글의 아이디

    @NotNull
    private Long reviewId;        // 리뷰 글의 아이디

    @NotNull
    private String loginId;       // 리뷰 글의 작성자 아이디

    @NotNull
    private String title;   // 리뷰 글의 제목
}
