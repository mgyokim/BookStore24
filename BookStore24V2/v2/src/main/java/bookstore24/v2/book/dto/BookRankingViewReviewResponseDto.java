package bookstore24.v2.book.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookRankingViewReviewResponseDto {

    private List<BookRankingViewReviewBookResponseDto> books; // 리뷰 조회수 합산 랭킹순으로 정렬한 책들

}
