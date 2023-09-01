package bookstore24.v2.book.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BookRankingViewReviewBookResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String author;

    @NotNull
    private String publisher;

    @NotNull
    private Double avgScore;

    @NotNull
    private String coverImg;

    @NotNull
    private Long isbn;
}
