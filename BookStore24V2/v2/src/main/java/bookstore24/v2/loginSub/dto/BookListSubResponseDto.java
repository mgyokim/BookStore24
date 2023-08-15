package bookstore24.v2.loginSub.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BookListSubResponseDto {

    @NotNull
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String author;

    @NotNull
    private String publisher;

    @NotNull
    private String coverImg;

    @NotNull
    private Long isbn;

    @NotNull
    private LocalDateTime createdDate;
}
