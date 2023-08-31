package bookstore24.v2.book.repository;

import bookstore24.v2.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // select * from member where isbn = ?
    public Book findByIsbn(Long isbn);

    // SELECT * FROM Book ORDER BY avg_score DESC;
    List<Book> findAllByOrderByAvgScoreDesc();

    // SELECT * FROM Book ORDER BY avg_score DESC LIMIT 10;
    List<Book> findTop10ByOrderByAvgScoreDesc();
}
