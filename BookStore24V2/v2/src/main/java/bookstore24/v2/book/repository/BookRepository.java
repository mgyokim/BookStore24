package bookstore24.v2.book.repository;

import bookstore24.v2.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // select * from member where isbn = ?
    public Book findByIsbn(Long isbn);

    // SELECT * FROM Book ORDER BY avg_score DESC;
    List<Book> findAllByOrderByAvgScoreDesc();

    // SELECT * FROM Book ORDER BY avg_score DESC LIMIT 10;
    List<Book> findTop10ByOrderByAvgScoreDesc();

    /**
     * SELECT b.*, SUM(r.view) AS total_view
     * FROM Book b
     * JOIN Review r ON b.book_id = r.book_id
     * GROUP BY b.book_id
     * ORDER BY total_view DESC
     * LIMIT 10
     */
    @Query(value = "SELECT b.*, SUM(r.view) AS total_view " +
            "FROM Book b " +
            "JOIN Review r ON b.book_id = r.book_id " +
            "GROUP BY b.book_id " +
            "ORDER BY total_view DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Book> findTop10ByOrderByTotalReviewViewDesc();


    /**
     * SELECT b.*, SUM(s.view) AS total_view
     * FROM Book b
     * JOIN Sell s ON b.book_id = s.book_id
     * GROUP BY b.book_id
     * ORDER BY total_view DESC
     * LIMIT 10
     */
    @Query(value = "SELECT b.*, SUM(s.view) AS total_view " +
            "FROM Book b " +
            "JOIN Sell s ON b.book_id = s.book_id " +
            "GROUP BY b.book_id " +
            "ORDER BY total_view DESC " +
            "LIMIT 10", // 10개의 결과만 제한
            nativeQuery = true)
    List<Book> findTop10ByOrderByTotalSellViewDesc();
}
