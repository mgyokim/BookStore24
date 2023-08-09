package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostSaveResponseDto {

    @NotNull
    private String loginId; // 작성자 loginId

    @NotNull
    private Long id;    // 저장된 판매글 id

    @NotNull
    private String title;   // 저장된 판매글 title
}
