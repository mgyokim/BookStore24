package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostDeleteRequestDto {

    @NotNull
    private Long sellId;    // 판매 글의 id

    @NotNull
    private String loginId;     // 판매 글 작성자의 loginId

    @NotNull
    private String title;      // 판매 글의 title

}
