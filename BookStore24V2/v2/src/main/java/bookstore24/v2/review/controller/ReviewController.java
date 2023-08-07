package bookstore24.v2.review.controller;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.domain.ReviewComment;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.review.dto.ReviewPostDetailRequestDto;
import bookstore24.v2.review.dto.ReviewPostDetailResponseDto;
import bookstore24.v2.review.dto.ReviewPostSaveRequestDto;
import bookstore24.v2.review.dto.ReviewPostSaveResponseDto;
import bookstore24.v2.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    @GetMapping("/review/post/detail")
    public ResponseEntity<?> reviewPostDetail(Authentication authentication, @RequestBody @Valid ReviewPostDetailRequestDto reviewPostDetailRequestDto) {
        log.info("[START] - ReviewController.reviewPostDetail / 도서 리뷰글 상세 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // ReviewPostDetailRequestDto 에서 리뷰글 작성자의 loginId, 리뷰글의 제목 title 을 얻기
        String reviewLoginId = reviewPostDetailRequestDto.getLoginId();
        String reviewTitle = reviewPostDetailRequestDto.getTitle();

        // 리뷰글 작성자의 loginId, 리뷰글의 제목 title 을 이용하여 해당하는 Review 글 찾기
        Review review = reviewService.findByLoginIdAndTitle(reviewLoginId, reviewTitle);
        log.info("로그인아이디와 타이틀로 찾은 리뷰" + review);

        // 리뷰글 상세 데이터 반환하기
        if (review == null) {   // reviewLoginId, reviewTitle 으로 해당하는 리뷰 글이 존재하지 않음.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 조건의 리뷰 글이 존재하지 않음");
        } else {
            // reviewLoginId, reviewTitle 으로 해당하는 리뷰 글의 조회수를 상세 글 데이터를 요청할 때마다 +1 해줌
            Long view = review.getView();
            if (view == null) { // 만약 해당 리뷰 글의 상세를 최초로 조회하는 것이라면,
                review.initView();  // 해당 리뷰 글의 view 를 0 으로 초기화
            }
            Long inquiryView = review.getView();    // 리뷰 글 상세를 조회하기 전의 view
            review.setView(inquiryView);            // 리뷰 글 상세를 조회 -> (리뷰 글 상세를 조회하기 전의 view)  + 1

            // 해당 리뷰글의 상세 데이터를 반환
            String title = review.getTitle();       // 리뷰 글 제목
            String content = review.getContent();   // 리뷰 글 본문
            Long nowView = review.getView();    // 리뷰 글 조회수
            Long score = review.getScore();     // 리뷰 글 평점
            LocalDateTime createdDate = review.getCreatedDate();    // 리뷰 글 작성일
            String writerNickname = review.getMember().getNickname(); // 리뷰 글 작성자 닉네임
            List<ReviewComment> reviewComments = review.getReviewComments();    // 리뷰 글 댓글

            String bookTitle = review.getBook().getTitle(); // 리뷰 도서 제목
            String author = review.getBook().getAuthor();   // 리뷰 도서 저자
            String publisher = review.getBook().getPublisher(); // 리뷰 도서 출판사
            String coverImg = review.getBook().getCoverImg();   // 리뷰 도서 커버이미지
            Long isbn = review.getBook().getIsbn(); // 리뷰 도서 isbn


            ReviewPostDetailResponseDto reviewPostDetailResponseDto = new ReviewPostDetailResponseDto();
            reviewPostDetailResponseDto.setTitle(title);
            reviewPostDetailResponseDto.setContent(content);
            reviewPostDetailResponseDto.setView(nowView);
            reviewPostDetailResponseDto.setScore(score);
            reviewPostDetailResponseDto.setCreatedDate(createdDate);
            reviewPostDetailResponseDto.setNickname(writerNickname);
            reviewPostDetailResponseDto.setReviewComments(reviewComments);
            reviewPostDetailResponseDto.setBookTitle(bookTitle);
            reviewPostDetailResponseDto.setAuthor(author);
            reviewPostDetailResponseDto.setPublisher(publisher);
            reviewPostDetailResponseDto.setCoverImg(coverImg);
            reviewPostDetailResponseDto.setIsbn(isbn);

            return ResponseEntity.status(HttpStatus.OK).body(reviewPostDetailResponseDto);
        }
    }

}
