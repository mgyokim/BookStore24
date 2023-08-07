package bookstore24.v2.book.repository;

import bookstore24.v2.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    // select * from member where isbn = ?
    public Book findByIsbn(Long isbn);
}
