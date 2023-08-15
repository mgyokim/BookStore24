package bookstore24.v2.loginSub.dto;

import bookstore24.v2.domain.SellStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class SellListSubResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private SellStatus status;

    @NotNull
    private Long price;

    @NotNull
    private Long view;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private Long bookId;

    @NotNull
    private Long memberId;
}
