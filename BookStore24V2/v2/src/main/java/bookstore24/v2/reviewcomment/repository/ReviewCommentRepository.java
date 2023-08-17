package bookstore24.v2.reviewcomment.repository;

import bookstore24.v2.domain.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

}
