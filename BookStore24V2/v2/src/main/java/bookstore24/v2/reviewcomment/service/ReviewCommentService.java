package bookstore24.v2.reviewcomment.service;

import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.domain.ReviewComment;
import bookstore24.v2.reviewcomment.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewCommentService {

    private final ReviewCommentRepository reviewCommentRepository;

    // ReviewComment 저장
    @Transactional
    public ReviewComment saveReviewComment(ReviewComment reviewComment, Member member, Review review) {
        ReviewComment savedReviewComment = reviewCommentRepository.save(reviewComment);
        reviewComment.connectingReviewCommentAndMember(member);
        reviewComment.connectingReviewCommentAndReview(review);
        return savedReviewComment;
    }

    // reviewId 로 reviewComment 조회
    public Optional<ReviewComment> findReviewCommentById(Long id) {
        Optional<ReviewComment> optionalReviewComment = reviewCommentRepository.findById(id);
        return optionalReviewComment;
    }

    @Transactional
    // ReviewComment 삭제
    public void deleteReviewCommentById(Long reviewCommentId) {
        Optional<ReviewComment> optionalReview = reviewCommentRepository.findById(reviewCommentId);
        if (optionalReview.isPresent()) {
            ReviewComment reviewComment = optionalReview.get();
            reviewComment.logicalDelete();     // ReviewComment 엔티티 deleted 필드를 true 로 변경하여 논리적 삭제 진행
        }
    }

    // memberId 로 reviewComment 들 조회
    public List<ReviewComment> findAllReviewCommentsByMemberId(Long memberId) {
        return reviewCommentRepository.findAllByMember_Id(memberId);
    }

}
