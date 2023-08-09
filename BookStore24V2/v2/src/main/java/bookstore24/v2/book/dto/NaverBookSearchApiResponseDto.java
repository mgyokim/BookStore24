package bookstore24.v2.book.dto;

import lombok.Data;

import java.util.List;

@Data
public class NaverBookSearchApiResponseDto {

    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<BookInformationSearchResponseDto> items;
}
