package bookstore24.v2.sell.repository;

import bookstore24.v2.domain.Sell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellRepository extends JpaRepository<Sell, Long> {

    // select * from sell where title = ?
    public Sell findByTitle(String title);

    // SELECT r FROM Sell r WHERE r.loginId = ?1 AND r.title = ?2
    public Sell findByMemberLoginIdAndTitle(String loginId, String title);
}
