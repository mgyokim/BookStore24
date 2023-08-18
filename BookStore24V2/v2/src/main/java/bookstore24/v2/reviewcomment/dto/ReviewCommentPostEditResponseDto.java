package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostEditResponseDto {

    @NotNull
    private Long reviewId;  // 리뷰 글 id

    @NotNull
    private String reviewTitle;   // 리뷰 글 title

    @NotNull
    private String reviewLoginId; // 리뷰 글 작성자 loginId

    @NotNull
    private Long reviewCommentId;     // 리뷰 댓글 id

    @NotNull
    private String reviewCommentLoginId;    // 리뷰 댓글 작성자 loginId

    @NotNull
    private String reviewCommentContent;     // 리뷰 댓글 content

}
