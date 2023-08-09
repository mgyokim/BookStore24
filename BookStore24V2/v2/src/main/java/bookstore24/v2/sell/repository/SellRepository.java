package bookstore24.v2.sell.repository;

import bookstore24.v2.domain.Sell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellRepository extends JpaRepository<Sell, Long> {

    // select * from sell where title = ?
    public Sell findByTitle(String title);

}
