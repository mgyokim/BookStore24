package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class SellPostEditResponseDto {

    @NotNull
    private String title;   // 판매 글 제목

    @NotNull
    private String bookTitle;   // 판매 도서 제목

    @NotNull
    private String author;  // 판매 도서 저자

    @NotNull
    private String publisher;   // 판매 도서 출판사

    @NotNull
    private String coverImg;    // 판매 도서 커버이미지

    @NotNull
    private Long isbn;      // 판매 도서 isbn

    @NotNull
    private String content;     // 판매 글 본문

    @NotNull
    private Long price;     // 판매 가격

    @NotNull
    private String talkUrl;     // 판매 채팅

    @NotNull
    private LocalDateTime createdDate;  // 판매 글 작성 날짜

    @NotNull
    private String nickname;    // 판매 글 작성자 닉네임
}
