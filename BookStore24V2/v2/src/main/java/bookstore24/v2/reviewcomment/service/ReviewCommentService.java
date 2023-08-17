package bookstore24.v2.reviewcomment.service;

import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.domain.ReviewComment;
import bookstore24.v2.reviewcomment.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
