package bookstore24.v2.review.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReviewPostDeleteRequestDto {

    @NotNull
    private Long id;      // 삭제를 요청하는 Review 의 id

    @NotNull
    private String loginId; // 삭제를 요청하는 Review 의 작성자 loginId

    @NotNull
    private String title;   // 삭제를 요청하는 Review 의 title

    private List<Long> reviewCommentIds;    // 삭제를 요청하는 Review 에 등록되어 있는 ReviewComment 들의 id

}
