package bookstore24.v2.sell.repository;

import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.domain.SellStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellRepository extends JpaRepository<Sell, Long> {

    // select * from sell where title = ?
    public Sell findByTitle(String title);

    // SELECT r FROM Sell r WHERE r.loginId = ?1 AND r.title = ?2
    public Sell findByMemberLoginIdAndTitle(String loginId, String title);

    // SELECT * FROM sell LIMIT ? OFFSET ?
    Page<Sell> findAll(Pageable pageable);

    // SELECT s FROM Sell s WHERE s.book.id = :bookId
    List<Sell> findAllByBook_Id(Long bookId);

    /**
     * SELECT
     *     sell0_.sell_id AS sell_id1_5_,
     *     sell0_.content AS content2_5_,
     *     sell0_.deleted AS deleted3_5_,
     *     sell0_.member_id AS member_i7_5_,
     *     sell0_.price AS price4_5_,
     *     sell0_.status AS status5_5_,
     *     sell0_.talk_url AS talk_url6_5_,
     *     sell0_.title AS title8_5_,
     *     sell0_.view AS view9_5_,
     *     sell0_.book_id AS book_id10_5_
     * FROM
     *     sell sell0_
     * INNER JOIN
     *     member member1_
     * ON
     *     sell0_.member_id=member1_.member_id
     * WHERE
     *     member1_.member_id=?
     *     AND sell0_.deleted = false;
     */
    List<Sell> findAllByMember_Id(Long memberId);

    // SELECT s FROM Sell s WHERE s.member = :member AND s.status = :status
    Page<Sell> findSellsByMemberAndStatus(Member member, SellStatus status, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.title LIKE %:keyword%
    Page<Sell> findByTitleContaining(String keyword, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.book.title LIKE %:keyword%
    Page<Sell> findByBook_TitleContaining(String keyword, Pageable pageable);
}
