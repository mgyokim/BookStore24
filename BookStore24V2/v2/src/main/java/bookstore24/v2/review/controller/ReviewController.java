package bookstore24.v2.review.controller;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.review.dto.ReviewPostSaveRequestDto;
import bookstore24.v2.review.dto.ReviewPostSaveResponseDto;
import bookstore24.v2.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final MemberService memberService;
    private final BookService bookService;
    private final ReviewService reviewService;

    @Transactional
    @PostMapping("/review/post/save")
    public ResponseEntity<?> reviewPostSave(Authentication authentication, @RequestBody @Valid ReviewPostSaveRequestDto reviewPostSaveRequestDto) {
        log.info("[START] - ReviewController.reviewPostSave / 도서 리뷰글 작성 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        String title = reviewPostSaveRequestDto.getTitle();
        String bookTitle = reviewPostSaveRequestDto.getBookTitle();
        String author = reviewPostSaveRequestDto.getAuthor();
        String publisher = reviewPostSaveRequestDto.getPublisher();
        Long score = reviewPostSaveRequestDto.getScore();
        String content = reviewPostSaveRequestDto.getContent();
        String coverImg = reviewPostSaveRequestDto.getCoverImg();

        Long isbn = reviewPostSaveRequestDto.getIsbn();

        // 데이터베이스에 해당 도서가 저장되어 있는지 isbn 으로 조회
        Book existStatusBook = bookService.findByIsbn(isbn);

        // 같은 제목으로 등록된 리뷰가 있는지 확인
        Review duplicateTitleReview = reviewService.duplicateTitleReview(title);

        if ((existStatusBook != null) & (duplicateTitleReview == null)) {  // 데이터베이스에 해당 책을 추가로 등록하지는 않음
            // 도서 리뷰글 생성 및 저장
            Review review = new Review(title, content, score);
            Review savedReview = reviewService.saveReview(review, member, existStatusBook);

            // ReviewPostSaveResponseDto 에 리뷰글 작성자의 loginId 입력
            ReviewPostSaveResponseDto reviewPostSaveResponseDto = new ReviewPostSaveResponseDto();
            reviewPostSaveResponseDto.setLoginId(JwtLoginId);

            // ReviewPostSaveResponseDto 에 리뷰글의 id 입력
            Long savedReviewId = savedReview.getId();
            reviewPostSaveResponseDto.setId(savedReviewId);

            // ReviewPostSaveResponseDto 에 리뷰 글의 제목 입력
            reviewPostSaveResponseDto.setTitle(title);

            log.info("[도서명 : " + bookTitle + "] 는 DB에 저장되어 있으므로 리뷰 글만 저장 완료");
            log.info("[END] - ReviewController.reviewPostSave / 도서 리뷰글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewPostSaveResponseDto);
        } else if ((existStatusBook == null) & (duplicateTitleReview == null)){    // 데이터베이스에 해당 책을 추가 등록
            // 데이터베이스에 해당 책 저장
            Book book = new Book(isbn, bookTitle, author, publisher, coverImg);
            Book savedBook = bookService.saveBook(book);

            // 도서 리뷰글 생성 및 저장
            Review review = new Review(title, content, score);
            Review savedReview = reviewService.saveReview(review, member, savedBook);

            // ReviewPostSaveResponseDto 에 리뷰 글 작성자의 loginId 입력
            ReviewPostSaveResponseDto reviewPostSaveResponseDto = new ReviewPostSaveResponseDto();
            reviewPostSaveResponseDto.setLoginId(JwtLoginId);

            // ReviewPostSaveResponseDto 에 리뷰 글의 id 입력
            Long savedReviewId = savedReview.getId();
            reviewPostSaveResponseDto.setId(savedReviewId);

            // ReviewPostSaveResponseDto 에 리뷰 글의 제목 입력
            reviewPostSaveResponseDto.setTitle(title);

            log.info("[도서명 : " + bookTitle + "] 는 DB에 저장되어 있지 않으므로 도서 + 리뷰 글 함께 저장 완료");
            log.info("[END] - ReviewController.reviewPostSave / 도서 리뷰글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewPostSaveResponseDto);
        }
        log.info("리뷰 글 제목 중복으로 저장 실패");
        log.info("[END] - ReviewController.reviewPostSave / 도서 리뷰글 작성 저장 요청 종료");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("리뷰 글 제목 중복으로 저장 실패");
    }


}
