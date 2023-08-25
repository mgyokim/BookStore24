package bookstore24.v2.review.service;

import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        Review duplicateTitleReview = reviewRepository.findByTitle(title);
        return duplicateTitleReview;
    }

    // 리뷰 글 작성자의 loginId 와 리뷰 글의 제목 title 을 이용하여 리뷰 글 찾기
    public Review findByLoginIdAndTitle(String loginId, String title) {
        Review review = reviewRepository.findByMemberLoginIdAndTitle(loginId, title);
        return review;
    }

    // 리뷰 글의 id 를 이용하여 리뷰 글 찾기
    public Optional<Review> findById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        return review;
    }

    // 전체 리뷰 글을 페이징하여 반환
    public Page<Review> getReviewList(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Transactional
    // review 삭제
    public void deleteReviewById(Long reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.logicalDelete();     // review 엔티티 deleted 필드를 true 로 변경하여 논리적 삭제 진행
            reviewRepository.save(review);
        }
    }

}
