package bookstore24.v2.sell.dto;

import bookstore24.v2.domain.SellStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class SellPostDetailResponseDto {

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long view;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private String nickname;

    @NotNull
    private Long price;

    @NotNull
    private String talkUrl;

    @NotNull
    private SellStatus status;

    @NotNull
    private String bookTitle;

    @NotNull
    private String author;

    @NotNull
    private String publisher;

    @NotNull
    private String coverImg;

    @NotNull
    private Long isbn;

}
