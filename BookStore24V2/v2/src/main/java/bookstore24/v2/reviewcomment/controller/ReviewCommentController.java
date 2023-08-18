package bookstore24.v2.reviewcomment.controller;

import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.domain.ReviewComment;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.review.service.ReviewService;
import bookstore24.v2.reviewcomment.dto.*;
import bookstore24.v2.reviewcomment.service.ReviewCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewCommentController {

    private final MemberService memberService;

    private final ReviewService reviewService;

    private final ReviewCommentService reviewCommentService;

    @Transactional
    @PostMapping("review/comment/post/save")
    public ResponseEntity<?> reviewCommentPostSave(Authentication authentication, @RequestBody @Valid ReviewCommentPostSaveRequestDto reviewCommentPostSaveRequestDto) {
        log.info("[START] - ReviewCommentController.reviewCommentPostSave / 댓글 작성 저장 요청 시작");

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
            log.info("[END] - ReviewCommentController.reviewCommentPostSave / 댓글 작성 저장 요청 종료");
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
            log.info("[END] - ReviewCommentController.reviewCommentPostSave / 댓글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostSaveResponseDto);
        }
    }

    @GetMapping("/review/comment/post/edit")
    public ResponseEntity<?> reviewCommentPostEdit(Authentication authentication, @RequestParam(value = "reviewId", required = true) Long reviewId, @RequestParam(value = "reviewCommentId", required = true) Long reviewCommentId) {
        log.info("[START] - ReviewCommentController.reviewCommentPostEdit / 댓글 작성 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 요청 params 로 보낸 reviewId 와 reviewCommentId 를 이용하여 이중 검증 진행
        // 검증 1. reviewId 의 reviewComments 에 요청 param 의 reviewCommentId 에 해당하는 댓글이 있는지 확인
        Optional<Review> matchReview = reviewService.findById(reviewId);
        if (!(matchReview.isPresent())) {   // matchReview 값이 존재하지 않으면, 잘못된 요청임
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 reviewId 에 해당하는 Review 가 존재하지 않음");
        } else {
            List<ReviewComment> reviewComments = matchReview.get().getReviewComments();

            Optional<ReviewComment> targetReviewCommentOptional = reviewComments.stream()
                    .filter(reviewComment -> reviewComment.getId().equals(reviewCommentId))
                    .findFirst();

            if (targetReviewCommentOptional.isPresent()) {  // 수정을 요청한 reviewComment 를 찾은 경우 -> // 검증 1을 통과임
                ReviewComment targetReviewComment = targetReviewCommentOptional.get();

                // 검증 2. reviewCommentId 에 해당하는 댓글을 작성한 사람의 loginId 가 JwtLoginId 와 같은지 검증(해당 댓글 작성자의 수정 요청인지 확인)
                String commentWriterLoginId = targetReviewComment.getMember().getLoginId();
                if (!(commentWriterLoginId.equals(JwtLoginId))) {   // 해당 댓글의 작성자 loginId 와 수정 요청을 한 회원의 loginId(JwtLoginId) 가 일치하지 않음
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("댓글 수정을 요청한 회원은 해당 댓글의 작성자가 아님");
                } else {    // 해당 댓글의 작성자 loginId 와 수정 요청을 한 회원의 loginId(JwtLoginId) 가 일치함

                    // 리뷰 글 id (Params)
                    String reviewTitle = matchReview.get().getTitle();// 리뷰 글 title
                    String reviewLoginId = matchReview.get().getMember().getLoginId();    // 리뷰 글 작성자 loginId
                    // 리뷰 댓글 id (Params)
                    String reviewCommentLoginId = targetReviewComment.getMember().getLoginId();// 리뷰 댓글 작성자 loginId
                    String reviewCommentContent = targetReviewComment.getContent();     // 리뷰 댓글 content

                    ReviewCommentPostEditResponseDto reviewCommentPostEditResponseDto = new ReviewCommentPostEditResponseDto();
                    reviewCommentPostEditResponseDto.setReviewId(reviewId);
                    reviewCommentPostEditResponseDto.setReviewTitle(reviewTitle);
                    reviewCommentPostEditResponseDto.setReviewLoginId(reviewLoginId);
                    reviewCommentPostEditResponseDto.setReviewCommentId(reviewCommentId);
                    reviewCommentPostEditResponseDto.setReviewCommentLoginId(reviewCommentLoginId);
                    reviewCommentPostEditResponseDto.setReviewCommentContent(reviewCommentContent);

                    return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostEditResponseDto);
                }
            } else {    // 수정을 요청한 reviewComment 를 찾지 못한 경우
                // 해당 reviewCommentId에 해당하는 ReviewComment 를 찾지 못한 경우
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 reviewCommentId 에 해당하는 ReviewComment 를 찾을 수 없음");
            }
        }
    }

    @Transactional
    @PostMapping("/review/comment/post/edit/save")
    public ResponseEntity<?> reviewCommentPostEditSave(Authentication authentication, @RequestBody @Valid ReviewCommentPostEditSaveRequestDto reviewCommentPostEditSaveRequestDto) {
        log.info("[START] - ReviewCommentController.reviewCommentPostEditSave / 댓글 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 요청 바디로 보낸 데이터
        Long requestBodyReviewId = reviewCommentPostEditSaveRequestDto.getReviewId(); // 댓글을 달려고 하는 글의 id
        String requestBodyLoginId = reviewCommentPostEditSaveRequestDto.getLoginId(); //  댓글을 달려고 하는 글의 작성자 loginId
        String requestBodyTitle = reviewCommentPostEditSaveRequestDto.getTitle(); // 수정하려는 댓글이 존재하는 글의 제목
        Long requestBodyReviewCommentId = reviewCommentPostEditSaveRequestDto.getReviewCommentId(); // 수정하려는 댓글의 id
        String requestBodyContent = reviewCommentPostEditSaveRequestDto.getContent(); // 댓글 본문

        // 요청 params 로 보낸 reviewId 와 reviewCommentId 를 이용하여 이중 검증 진행
        // 검증 1. reviewId 의 reviewComments 에 요청 param 의 reviewCommentId 에 해당하는 댓글이 있는지 확인
        Optional<Review> matchReview = reviewService.findById(requestBodyReviewId);

        if (!(matchReview.isPresent())) {   // matchReview 값이 존재하지 않으면, 잘못된 요청임

            log.info("해당 reviewId 에 해당하는 Review 가 존재하지 않음 -> 댓글 수정 실패");
            log.info("[END] - ReviewCommentController.reviewCommentPostEditSave / 댓글 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 reviewId 에 해당하는 Review 가 존재하지 않음");
        } else {
            List<ReviewComment> reviewComments = matchReview.get().getReviewComments();

            Optional<ReviewComment> targetReviewCommentOptional = reviewComments.stream()
                    .filter(reviewComment -> reviewComment.getId().equals(requestBodyReviewCommentId))
                    .findFirst();

            if (targetReviewCommentOptional.isPresent()) {  // 수정을 요청한 reviewComment 를 찾은 경우 -> // 검증 1을 통과임
                ReviewComment targetReviewComment = targetReviewCommentOptional.get();

                // 검증 2. reviewCommentId 에 해당하는 댓글을 작성한 사람의 loginId 가 JwtLoginId 와 같은지 검증(해당 댓글 작성자의 수정 요청인지 확인)
                String commentWriterLoginId = targetReviewComment.getMember().getLoginId();
                if (!(commentWriterLoginId.equals(JwtLoginId))) {   // 해당 댓글의 작성자 loginId 와 수정 요청을 한 회원의 loginId(JwtLoginId) 가 일치하지 않음

                    log.info("댓글 수정을 요청한 회원은 해당 댓글의 작성자가 아님 -> 댓글 수정 실패");
                    log.info("[END] - ReviewCommentController.reviewCommentPostEditSave / 댓글 수정 저장 요청 종료");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("댓글 수정을 요청한 회원은 해당 댓글의 작성자가 아님");
                } else {    // 해당 댓글의 작성자 loginId 와 수정 요청을 한 회원의 loginId(JwtLoginId) 가 일치함

                    // 댓글 수정 저장을 진행
                    targetReviewComment.editContent(requestBodyContent);

                    ReviewCommentPostEditSaveResponseDto reviewCommentPostEditSaveResponseDto = new ReviewCommentPostEditSaveResponseDto();
                    reviewCommentPostEditSaveResponseDto.setLoginId(requestBodyLoginId);
                    reviewCommentPostEditSaveResponseDto.setTitle(requestBodyTitle);

                    log.info("[reviewCommentId : " + requestBodyReviewCommentId + "] 에 대한 댓글 수정 완료함 -> 댓글 수정 성공");
                    log.info("[END] - ReviewCommentController.reviewCommentPostEditSave / 댓글 수정 저장 요청 종료");
                    return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostEditSaveResponseDto);
                }
            } else {    // 수정을 요청한 reviewComment 를 찾지 못한 경우
                // 해당 reviewCommentId에 해당하는 ReviewComment 를 찾지 못한 경우

                log.info("해당 reviewCommentId 에 해당하는 ReviewComment 를 찾을 수 없음 -> 댓글 수정 실패");
                log.info("[END] - ReviewCommentController.reviewCommentPostEditSave / 댓글 수정 저장 요청 종료");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 reviewCommentId 에 해당하는 ReviewComment 를 찾을 수 없음");
            }
        }
    }

}
