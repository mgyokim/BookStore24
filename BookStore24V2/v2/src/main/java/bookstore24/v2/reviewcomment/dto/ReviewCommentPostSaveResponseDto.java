package bookstore24.v2.reviewcomment.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewCommentPostSaveResponseDto {

    @NotNull
    private String loginId;

    @NotNull
    private String title;
}
