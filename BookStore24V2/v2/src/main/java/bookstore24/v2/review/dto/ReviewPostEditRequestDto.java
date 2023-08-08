package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewPostEditRequestDto {

    @NotNull
    private String loginId;

    @NotNull
    private String title;
}
