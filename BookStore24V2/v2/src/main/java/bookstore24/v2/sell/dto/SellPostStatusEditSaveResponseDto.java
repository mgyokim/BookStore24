package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostStatusEditSaveResponseDto {

    @NotNull
    private String loginId;

    @NotNull
    private String title;
}
