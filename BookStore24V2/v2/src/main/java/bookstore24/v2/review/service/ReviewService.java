package bookstore24.v2.review.service;

import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    // 특정 bookId 를 포함하는 Review 찾기
    public List<Review> findReviewsByBookId(Long bookId) {
        return reviewRepository.findAllByBook_Id(bookId);
    }

    // memberId 로 Review 찾기
    public List<Review> findAllReviewsByMemberId(Long memberId) {
        return reviewRepository.findAllByMember_Id(memberId);
    }

    // member 로 Review 찾기 (페이징)
    public Page<Review> findReviewsByMember(Member member, Pageable pageable) {
        return reviewRepository.findReviewsByMember(member, pageable);
    }

    // Title 로 Review 를 검색하고 페이지네이션 적용
    public Page<Review> searchReviewsByTitleKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Review> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Review> reviews = reviewRepository.findByTitleContaining(keyword, pageable);
            result.addAll(reviews.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Review> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

    // Book.title 로 Review 를 검색하고 페이지네이션 적용
    public Page<Review> searchReviewsByBookTitle(String keyword, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keyword.split("\\s+");
        Set<Review> result = new HashSet<>(); // 중복 제거용 Set
        for (String kw : keywordArray) {
            Page<Review> reviews = reviewRepository.findByBook_TitleContaining(kw, pageable);
            result.addAll(reviews.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Review> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

    // Book.author 로 Review 를 검색하고 페이지네이션 적용
    public Page<Review> searchReviewsByAuthorKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Review> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Review> reviews = reviewRepository.findByBook_AuthorContaining(keyword, pageable);
            result.addAll(reviews.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Review> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

    // Member.nickname 로 Review 를 검색하고 페이지네이션 적용
    public Page<Review> searchReviewsByMemberNicknameKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Review> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Review> reviews = reviewRepository.findByMember_NicknameContaining(keyword, pageable);
            result.addAll(reviews.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Review> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

}
