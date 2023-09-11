package bookstore24.v2.sell.dto;

import bookstore24.v2.domain.SellStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class SellPostListSearchByBookTitleResponseDto {

    @NotNull
    private Long id;            // 판매 글 아이디

    @NotNull
    private String title;       // 판매 글 제목

    @NotNull
    private SellStatus status;  // 판매 글 상태

    @NotNull
    private String coverImg;    // 판매 글 등록 도서 커버 이미지

    @NotNull
    private String bookTitle;   // 판매 글 등록 도서 제목

    @NotNull
    private String author;      // 판매 글 등록 도서 저자

    @NotNull
    private String publisher;   // 판매 글 등록 도서 출판사

    @NotNull
    private Long price;         // 판매 글 가격

    @NotNull
    private String nickname;    // 판매 글 등록 회원 닉네임

    @NotNull
    private String loginId; // 판매 글 등록 회원 로그인아이디

    @NotNull
    private LocalDateTime createdDate;    // 판매 글 작성 날짜

    @NotNull
    private Long view;    // 판매 글 조회수
}
