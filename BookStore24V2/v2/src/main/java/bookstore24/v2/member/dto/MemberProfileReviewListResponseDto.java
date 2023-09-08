package bookstore24.v2.member.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class MemberProfileReviewListResponseDto {

    @NotNull
    private Long id;          // 리뷰 글 아이디

    @NotNull
    private String title;       // 리뷰 글 제목

    @NotNull
    private Long score;         // 리뷰 글 평점

    @NotNull
    private String coverImg;    // 리뷰 글 등록 도서 커버 이미지

    @NotNull
    private String bookTitle;   // 리뷰 글 등록 도서 제목

    @NotNull
    private String author;      // 리뷰 글 등록 도서 저자

    @NotNull
    private String publisher;   // 리뷰 글 등록 도서 출판사

    @NotNull
    private String nickname;    // 리뷰 글 등록 회원 닉네임

    @NotNull
    private String loginId;     // 리뷰 글 등록 회원 로그인아이디

    @NotNull
    private LocalDateTime createdDate;    // 리뷰 글 작성 날짜

    @NotNull
    private Long view;    // 리뷰 글 조회수
}
