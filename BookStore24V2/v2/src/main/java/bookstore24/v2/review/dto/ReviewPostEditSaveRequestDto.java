package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReviewPostEditSaveRequestDto {

    @NotNull
    private String title;       // 리뷰 글 제목

    @NotNull
    private String bookTitle;   // 리뷰 도서 제목

    @NotNull
    private String author;      // 리뷰 도서 저자

    @NotNull
    private String publisher;   // 리뷰 도서 출판사

    @NotNull
    private String coverImg;    // 리뷰 도서 커버이미지

    @NotNull
    private Long isbn;        // 리뷰 도서 isbn

    @NotNull
    private String content;     // 리뷰 글 본문

    @NotNull
    private Long score;       // 리뷰 글 평점
}
