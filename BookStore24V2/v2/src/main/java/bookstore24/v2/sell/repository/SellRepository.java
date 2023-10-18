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

    // SELECT * FROM sell WHERE member_id = :memberId AND status = :status ORDER BY created_date DESC
    Page<Sell> findSellsByMemberAndStatusOrderByCreatedDateDesc(Member member, SellStatus status, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.title LIKE %:keyword%
    Page<Sell> findByTitleContaining(String keyword, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.book.title LIKE %:keyword%
    Page<Sell> findByBook_TitleContaining(String keyword, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.book.author LIKE %:author%
    Page<Sell> findByBook_AuthorContaining(String author, Pageable pageable);

    // SELECT r FROM Sell r WHERE r.member.nickname LIKE %:keyword%
    Page<Sell> findByMember_NicknameContaining(String keyword, Pageable pageable);

    // SELECT COUNT(*) FROM Sell s WHERE s.title LIKE '%검색어%'
    long countByTitleContaining(String keyword);

    // SELECT COUNT(*) FROM sell s WHERE s.book.title LIKE %:keyword%
    long countByBook_TitleContaining(String keyword);

    //SELECT COUNT(sell.id) FROM Sell s WHERE s.book.author LIKE %:keyword%
    long countByBook_AuthorContaining(String keyword);

    // SELECT COUNT(s) FROM Sell s WHERE s.member.nickname LIKE %:keyword%
    long countByMember_NicknameContaining(String keyword);

    // SELECT s FROM Sell s WHERE s.status = :status
    Page<Sell> findByStatus(SellStatus status, Pageable pageable);
}
