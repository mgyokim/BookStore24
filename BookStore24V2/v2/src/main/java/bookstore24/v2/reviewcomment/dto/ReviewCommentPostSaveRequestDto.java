package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostSaveRequestDto {

    @NotNull
    private Long id;    // 댓글을 달려고 하는 글의 id

    @NotNull
    private String loginId; // 댓글을 달려고 하는 글의 작성자 loginId

    @NotNull
    private String title;   // 댓글을 달려고 하는 글의 제목

    @NotNull
    private String content; // 댓글 본문

}
