package bookstore24.v2.reviewcomment.controller;

import bookstore24.v2.domain.ReviewComment;
import bookstore24.v2.reviewcomment.dto.ReviewCommentPostSaveRequestDto;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.review.service.ReviewService;
import bookstore24.v2.reviewcomment.dto.ReviewCommentPostSaveResponseDto;
import bookstore24.v2.reviewcomment.service.ReviewCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewCommentController {

    private final MemberService memberService;

    private final ReviewService reviewService;

    private final ReviewCommentService reviewCommentService;

    @Transactional
    @PostMapping("/comment/post/save")
    public ResponseEntity<?> reviewCommentPostSave(Authentication authentication, @RequestBody @Valid ReviewCommentPostSaveRequestDto reviewCommentPostSaveRequestDto) {
        log.info("[START] - ReviewCommentController.commentPostSave / 댓글 작성 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 요청 바디로 보낸 데이터
        Long requestBodyId = reviewCommentPostSaveRequestDto.getId(); // 댓글을 달려고 하는 글의 id
        String requestBodyLoginId = reviewCommentPostSaveRequestDto.getLoginId(); //  댓글을 달려고 하는 글의 작성자 loginId
        String requestBodyTitle = reviewCommentPostSaveRequestDto.getTitle(); // 댓글을 달려고 하는 글의 제목
        String requestBodyContent = reviewCommentPostSaveRequestDto.getContent(); // 댓글 본문

        // 댓글 작성 저장 요청을 보낸 게시글의 존재하는지 검증
        Review matchPost = reviewService.findByLoginIdAndTitle(requestBodyLoginId, requestBodyTitle);   // 요청 바디 조건으로 찾은 게시 글
        // matchPost 의 id 가 requestBodyId 와 일치하는지 검증
        if (!(matchPost.getId().equals(requestBodyId))) {   // 만약 matchPost 의 id 가 requestBodyId 와 일치하지 않으면 검증 실패

            log.info("[댓글 작성 게시글 id: " + requestBodyId + ", 작성 댓글 내용 : " + requestBodyContent + "] 저장 실패");
            log.info("[END] - ReviewCommentController.commentPostSave / 댓글 작성 저장 요청 종료");
            // 댓글 저장 요청 실패 응답 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("댓글 작성 저장을 요청한 Body 의 조건에 해당하는 게시 글이 없음");
        } else {    // matchPost 의 id 가 requestBodyId 와 일치하여 검증 성공
            // 댓글 생성
            ReviewComment reviewComment = new ReviewComment(requestBodyContent);
            // 해당 게시 글에 댓글 저장
            ReviewComment savedReviewComment = reviewCommentService.saveReviewComment(reviewComment, member, matchPost);

            // 댓글 저장 요청 성공 응답 반환
            ReviewCommentPostSaveResponseDto reviewCommentPostSaveResponseDto = new ReviewCommentPostSaveResponseDto();
            reviewCommentPostSaveResponseDto.setLoginId(requestBodyLoginId);
            reviewCommentPostSaveResponseDto.setTitle(requestBodyTitle);

            log.info("[댓글 작성 게시글 id: " + requestBodyId + ", 작성 댓글 내용 : " + requestBodyContent + "] 저장 성공");
            log.info("[END] - ReviewCommentController.commentPostSave / 댓글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostSaveResponseDto);
        }
    }
}
