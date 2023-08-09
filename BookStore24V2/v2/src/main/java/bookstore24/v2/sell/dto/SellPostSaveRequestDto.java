package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostSaveRequestDto {

    @NotNull
    private String title;   // 판매 글 제목

    @NotNull
    private String bookTitle;   // 판매 책 제목

    @NotNull
    private String author;  // 저자

    @NotNull
    private String publisher;   // 출판사

    @NotNull
    private Long isbn;    // 도서 isbn

    @NotNull
    private String coverImg;    // 도서 커버이미지

    @NotNull
    private String talkUrl;     // 채팅방 링크

    @NotNull
    private Long price;     // 가격

    @NotNull
    private String content; // 본문
}
