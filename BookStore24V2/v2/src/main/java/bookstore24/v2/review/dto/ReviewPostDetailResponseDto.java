package bookstore24.v2.review.dto;

import bookstore24.v2.domain.ReviewComment;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewPostDetailResponseDto {

    @NotNull
    private Long id;    // 리뷰 글의 아이디

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
    private Long view;        // 리뷰 글 조회수

    @NotNull
    private Long score;       // 리뷰 글 평점

    @NotNull
    private LocalDateTime createdDate;  // 리뷰 글 작성 날짜

    @NotNull
    private String nickname;     // 리뷰 글 작성자 닉네임

    @NotNull
    private String loginId;    // 리뷰 글 작성자 로그인 아이디

    private List<ReviewPostDetailReviewCommentResponseDto> reviewComments; // 리뷰 글 댓글

}
