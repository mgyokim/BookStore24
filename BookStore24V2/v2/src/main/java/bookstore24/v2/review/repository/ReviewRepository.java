package bookstore24.v2.review.repository;

import bookstore24.v2.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // select * from review where title = ?
    public Review findByTitle(String title);

    // SELECT r FROM Review r WHERE r.loginId = ?1 AND r.title = ?2
    Review findByMemberLoginIdAndTitle(String loginId, String title);


}
