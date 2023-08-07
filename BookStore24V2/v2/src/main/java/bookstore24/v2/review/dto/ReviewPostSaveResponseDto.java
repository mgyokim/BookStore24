package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewPostSaveResponseDto {

    @NotNull
    private String loginId; // 작성자 loginId

    @NotNull
    private Long id;    // 저장된 리뷰글 id

    @NotNull
    private String title;   // 저장된 리뷰글 title
}
