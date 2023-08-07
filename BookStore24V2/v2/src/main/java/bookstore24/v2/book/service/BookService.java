package bookstore24.v2.book.service;

import bookstore24.v2.book.repository.BookRepository;
import bookstore24.v2.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
