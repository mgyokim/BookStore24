package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewPostSaveRequestDto {

    @NotNull
    private String title;   // 리뷰글 제목

    @NotNull
    private Long isbn;    // 도서 isbn

    @NotNull
    private String bookTitle;   // 도서 제목

    @NotNull
    private String author;  // 도서 저자

    @NotNull
    private String publisher;   // 도서 출판사

    @NotNull
    private String coverImg;    // 도서 커버이미지

    @NotNull
    private String content; // 리뷰글 본문

    @NotNull
    private Long score;   // 도서 리뷰 평점

}
