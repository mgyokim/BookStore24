package bookstore24.v2.reviewcomment.repository;

import bookstore24.v2.domain.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    /**
     * SELECT
     *     reviewcomment0_.ReviewComment_id AS ReviewCo1_4_,
     *     reviewcomment0_.content AS content2_4_,
     *     reviewcomment0_.member_id AS member_i4_4_,
     *     reviewcomment0_.review_id AS review_i5_4_
     * FROM
     *     ReviewComment reviewcomment0_
     * WHERE
     *     reviewcomment0_.member_id = ?
     */
    List<ReviewComment> findAllByMember_Id(Long memberId);

}
