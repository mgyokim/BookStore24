package bookstore24.v2.loginSub.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ReviewListSubResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long score;

    @NotNull
    private Long view;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private Long bookId;

    @NotNull
    private Long memberId;
}
