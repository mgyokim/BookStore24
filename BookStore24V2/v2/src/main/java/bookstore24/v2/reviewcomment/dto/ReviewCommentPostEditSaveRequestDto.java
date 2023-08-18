package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostEditSaveRequestDto {

    @NotNull
    private Long reviewId;    // 수정하려는 댓글이 존재하는 글의 id

    @NotNull
    private String loginId; // 수정하려는 댓글이 존재하는 글의 작성자 loginId

    @NotNull
    private String title;   // 수정하려는 댓글이 존재하는 글의 제목

    @NotNull
    private Long reviewCommentId;     // 수정하려는 댓글의 id

    @NotNull
    private String content; // 수정해서 작성한 댓글 본문

}
