package bookstore24.v2.sell.repository;

import bookstore24.v2.domain.Sell;
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
}
