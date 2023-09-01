package bookstore24.v2.book.service;

import bookstore24.v2.book.repository.BookRepository;
import bookstore24.v2.domain.Book;
import bookstore24.v2.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

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

    // 책 목록을 리뷰 평균 평점을 기준으로 정렬하여 반환
    public List<Book> findAllBooksOrderByAverageScoreDesc() {
        List<Book> books = bookRepository.findAllByOrderByAvgScoreDesc();
        return books;
    }

    // 책 목록을 리뷰 평균 평점을 기준으로 정렬하여 상위 10개의 책을 반환
    public List<Book> findTop10BooksOrderByAverageScoreDesc() {
        List<Book> top10Books = bookRepository.findTop10ByOrderByAvgScoreDesc();

        if (top10Books.size() < 10) {
            // 만약 책이 10개 미만이면, 다른 동작을 수행하거나 예외 처리를 할 수 있음
            // 예를 들어 남은 책을 그대로 반환하거나 예외를 던지는 등의 처리 가능
            // 여기서는 그대로 반환하도록 처리함
            return top10Books;
        }

        return top10Books;
    }

    // 리뷰들 중에서 동일한 책을 가진 리뷰의 view 값을 합산하여 Book 을 내림차순으로 정렬
    public List<Book> findTop10BooksOrderByTotalReviewViewDesc() {
        return bookRepository.findTop10ByOrderByTotalReviewViewDesc();
    }
}
