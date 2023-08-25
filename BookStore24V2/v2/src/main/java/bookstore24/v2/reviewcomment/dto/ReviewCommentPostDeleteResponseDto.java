package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostDeleteResponseDto {

    @NotNull
    private String loginId; // review 작성자의 loginId

    @NotNull
    protected String title; // review 의 title
}
