package bookstore24.v2.book.service;

import bookstore24.v2.book.repository.BookRepository;
import bookstore24.v2.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    // book 저장
    public Book saveBook(Book book) {
        Book savedBook = bookRepository.save(book);
        return savedBook;
    }

    // isbn 으로 도서 조회
    public Book findByIsbn(Long isbn) {
        Book book = bookRepository.findByIsbn(isbn);
        return book;
    }

    @Transactional
    // Book 삭제
    public void deleteBookById(Long bookId) {
        Optional<Book> optionalReview = bookRepository.findById(bookId);
        if (optionalReview.isPresent()) {
            Book book = optionalReview.get();
            book.logicalDelete();     // book 엔티티 deleted 필드를 true 로 변경하여 논리적 삭제 진행
        }
    }
}
