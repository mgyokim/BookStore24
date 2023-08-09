package bookstore24.v2.book.dto;

import lombok.Data;

@Data
public class BookInformationSearchResponseDto {

    private String title;
    private String link;
    private String image;
    private String author;
    private String discount;
    private String publisher;
    private String pubdate;
    private String isbn;
    private String description;
}
