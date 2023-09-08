package bookstore24.v2.review.repository;

import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // select * from review where title = ?
    public Review findByTitle(String title);

    // SELECT r FROM Review r WHERE r.loginId = ?1 AND r.title = ?2
    public Review findByMemberLoginIdAndTitle(String loginId, String title);

    // SELECT * FROM review LIMIT ? OFFSET ?
    Page<Review> findAll(Pageable pageable);

    // SELECT r FROM Review r WHERE r.book.id = :bookId
    List<Review> findAllByBook_Id(Long bookId);

    // SELECT r FROM Review r WHERE r.member.id = :memberId
    List<Review> findAllByMember_Id(Long memberId);

    // SELECT r FROM Review r WHERE r.member = :member
    Page<Review> findReviewsByMember(Member member, Pageable pageable);

}
