package bookstore24.v2.review.repository;

import bookstore24.v2.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // select * from member where title = ?
    public Review findByTitle(String title);
}
