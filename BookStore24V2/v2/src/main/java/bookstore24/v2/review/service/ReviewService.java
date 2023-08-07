package bookstore24.v2.review.service;

import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    // review 저장
    public Review saveReview(Review review, Member member, Book book) {
        Review savedReview = reviewRepository.save(review);
        review.connectingReviewAndBook(book);
        review.connectingReviewAndMember(member);
        return savedReview;
    }

    // review title 중복 확인
    public Review duplicateTitleReview(String title) {
        Review duplicateReviewTitle = reviewRepository.findByTitle(title);
        return duplicateReviewTitle;
    }

    // 리뷰 글 작성자의 loginId 와 리뷰 글의 제목 title 을 이용하여 리뷰 글 찾기
    public Review findByLoginIdAndTitle(String loginId, String title) {
        Review review = reviewRepository.findByMemberLoginIdAndTitle(loginId, title);
        return review;
    }
}
