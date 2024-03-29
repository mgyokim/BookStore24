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
        log.info("[START] - ReviewCommentController.reviewCommentPostEdit / 댓글 수정 권한 확인 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 요청 params 로 보낸 reviewId 와 reviewCommentId 를 이용하여 이중 검증 진행
        // 검증 1. reviewId 의 reviewComments 에 요청 param 의 reviewCommentId 에 해당하는 댓글이 있는지 확인
        Optional<Review> matchReview = reviewService.findById(reviewId);
        if (!(matchReview.isPresent())) {   // matchReview 값이 존재하지 않으면, 잘못된 요청임

            log.info("해당 reviewId 에 해당하는 Review 가 존재하지 않음 -> 수정 권한 없음");
            log.info("[END] - ReviewCommentController.reviewCommentPostEdit / 댓글 수정 권한 확인 요청 종료");
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

                    log.info("댓글 수정을 요청한 회원은 해당 댓글의 작성자가 아님 -> 수정 권한 없음");
                    log.info("[END] - ReviewCommentController.reviewCommentPostEdit / 댓글 수정 권한 확인 요청 종료");
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

                    log.info("[reviewCommentId : " + reviewCommentId + "] 를 [JwtLoginId : " + JwtLoginId +"] 가 수정 가능 -> 수정 권한 OK");
                    log.info("[END] - ReviewCommentController.reviewCommentPostEdit / 댓글 수정 권한 확인 요청 종료");
                    return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostEditResponseDto);
                }
            } else {    // 수정을 요청한 reviewComment 를 찾지 못한 경우
                // 해당 reviewCommentId에 해당하는 ReviewComment 를 찾지 못한 경우
                log.info("[END] - ReviewCommentController.reviewCommentPostEdit / 댓글 수정 권한 확인 요청 종료");
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

    @PostMapping("/review/comment/post/delete")
    public ResponseEntity<?> reviewCommentPostDelete(Authentication authentication, @RequestBody @Valid ReviewCommentPostDeleteRequestDto reviewCommentPostDeleteRequestDto) {
        log.info("[START] - ReviewCommentController.reviewCommentPostDelete / 댓글 단건 삭제 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        Long reviewCommentId = reviewCommentPostDeleteRequestDto.getReviewCommentId();  // 삭제 요청하는 reviewComment 의 id
        Long reviewId = reviewCommentPostDeleteRequestDto.getReviewId();        // 삭제 요청하는 댓글이 저장되어 있는 review 의 id
        String reviewLoginId = reviewCommentPostDeleteRequestDto.getLoginId();    // 삭제 요청하는 댓글이 저장되어 있는 review 작성자의 loginId
        String reviewTitle = reviewCommentPostDeleteRequestDto.getTitle();      // 삭제 요청하는 댓글이 저장되어 있는 review 의 title

        // 요청으로 전달받은 reviewCommentId 를 이용하여 해당하는 reviewComment 찾기
        Optional<ReviewComment> reviewCommentById = reviewCommentService.findReviewCommentById(reviewCommentId);
        if (reviewCommentById.isPresent()) {
            ReviewComment reviewComment = reviewCommentById.get();
            Long matchReviewId = reviewComment.getReview().getId();
            if (!(matchReviewId.equals(reviewId))) {   // 요청 Body 의 reviewId 와 matchReviewId 가 다르면 잘못된 요청임
                log.info("[END] - ReviewCommentController.reviewCommentPostDelete / 댓글 단건 삭제 요청 종료");
                log.info("reviewId 와 reviewCommentId 매칭 실패, 댓글 삭제 실패");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("reviewId 와 reviewCommentId 매칭 실패");
            }
            // reviewComment 를 활용한 로직 수행
            // 해당 reviewComment 의 작성자가 JwtLoginId 와 일치하는지 검증
            String reviewCommentLoginId = reviewComment.getMember().getLoginId();
            if (JwtLoginId.equals(reviewCommentLoginId)) {  // 해당 reviewComment 를 작성한 작성자의 요청이면
                reviewCommentService.deleteReviewCommentById(reviewCommentId);
                ReviewCommentPostDeleteResponseDto reviewCommentPostDeleteResponseDto = new ReviewCommentPostDeleteResponseDto();
                reviewCommentPostDeleteResponseDto.setLoginId(reviewLoginId);
                reviewCommentPostDeleteResponseDto.setTitle(reviewTitle);
                log.info("댓글 삭제 성공");
                log.info("[END] - ReviewCommentController.reviewCommentPostDelete / 댓글 단건 삭제 요청 종료");
                return ResponseEntity.status(HttpStatus.OK).body(reviewCommentPostDeleteResponseDto);
            } else {    // 해당 reviewComment 를 작성한 작성자의 요청이 아님
                log.info("해당 댓글에 대한 삭제 권한이 없는 회원의 요청임, 댓글 삭제 실패");
                log.info("[END] - ReviewCommentController.reviewCommentPostDelete / 댓글 단건 삭제 요청 종료");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 댓글에 대한 삭제 권한이 없는 회원의 요청임");
            }
        } else {
            // reviewCommentId에 해당하는 ReviewComment 가 존재하지 않는 경우에 대한 처리
            log.info("ReviewComment NotFound 로 댓글 삭제 실패");
            log.info("[END] - ReviewCommentController.reviewCommentPostDelete / 댓글 단건 삭제 요청 종료");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ReviewComment NotFound");
        }
    }

}
