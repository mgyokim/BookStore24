package bookstore24.v2.book.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookRankingScoreResponseDto {

    private List<BookRankingScoreBookResponseDto> books; // 평점 랭킹순으로 정렬한 책들
}
