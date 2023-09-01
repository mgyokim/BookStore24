package bookstore24.v2.book.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookRankingViewSellResponseDto {

    private List<BookRankingViewSellBookResponseDto> books; // 판매 조회수 합산 랭킹순으로 정렬한 책들

}
