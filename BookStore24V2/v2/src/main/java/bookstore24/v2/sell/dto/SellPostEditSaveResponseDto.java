package bookstore24.v2.sell.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SellPostEditSaveResponseDto {

    @NotNull
    private String loginId;

    @NotNull
    private String title;
}