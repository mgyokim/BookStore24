package bookstore24.v2.review.controller;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.*;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.review.dto.*;
import bookstore24.v2.review.service.ReviewService;
import bookstore24.v2.reviewcomment.service.ReviewCommentService;
import bookstore24.v2.sell.service.SellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final MemberService memberService;
    private final BookService bookService;
    private final ReviewService reviewService;
    private final ReviewCommentService reviewCommentService;
    private final SellService sellService;

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
        } else if ((existStatusBook == null) & (duplicateTitleReview == null)) {    // 데이터베이스에 해당 책을 추가 등록
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
    public ResponseEntity<?> reviewPostDetail(Authentication authentication, @RequestParam(value = "loginId", required = true) String reviewPostWriterLoginId, @RequestParam(value = "title", required = true) String reviewPostTitle) {
        log.info("[START] - ReviewController.reviewPostDetail / 도서 리뷰글 상세 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 리뷰글 작성자의 loginId, 리뷰글의 제목 title 을 이용하여 해당하는 Review 글 찾기
        Review review = reviewService.findByLoginIdAndTitle(reviewPostWriterLoginId, reviewPostTitle);
        log.info("로그인 아이디와 타이틀로 찾은 리뷰 : " + review);

        // 리뷰글 상세 데이터 반환하기
        if (review == null) {   // reviewLoginId, reviewTitle 으로 해당하는 리뷰 글이 존재하지 않음.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 조건의 리뷰 글이 존재하지 않음");
        } else {
            // reviewLoginId, reviewTitle 으로 해당하는 리뷰 글의 조회수를 상세 글 데이터를 요청할 때마다 +1 해줌
            Long view = review.getView();
            if (view == null) { // 만약 해당 리뷰 글의 상세를 최초로 조회하는 것이라면,
                log.info("[리뷰 작성자의 로그인 아이디 : " + reviewPostWriterLoginId + ", 리뷰 글의 제목 : " + reviewPostTitle + "] 도서 리뷰글 상세가 최초로 요청됨. 조회수 0으로 초기화 완료");
                review.initView();  // 해당 리뷰 글의 view 를 0 으로 초기화
            }
            Long inquiryView = review.getView();    // 리뷰 글 상세를 조회하기 전의 view
            review.setView(inquiryView);            // 리뷰 글 상세를 조회 -> (리뷰 글 상세를 조회하기 전의 view)  + 1
            log.info("[리뷰 작성자의 로그인 아이디 : " + reviewPostWriterLoginId + ", 리뷰 글의 제목 : " + reviewPostTitle + "] 리뷰 글 조회수 : " + review.getView() + " 로 업데이트 완료");

            // 해당 리뷰글의 상세 데이터를 반환
            Long id = review.getId();   // 리뷰 글 아이디
            String title = review.getTitle();       // 리뷰 글 제목
            String content = review.getContent();   // 리뷰 글 본문
            Long nowView = review.getView();    // 리뷰 글 조회수
            Long score = review.getScore();     // 리뷰 글 평점
            LocalDateTime createdDate = review.getCreatedDate();    // 리뷰 글 작성일
            String writerNickname = review.getMember().getNickname(); // 리뷰 글 작성자 닉네임
            String writerLoginId = review.getMember().getLoginId(); // 리뷰 글 작성자 로그인 아이디
            List<ReviewComment> reviewComments = review.getReviewComments();    // 리뷰 글 댓글

            String bookTitle = review.getBook().getTitle(); // 리뷰 도서 제목
            String author = review.getBook().getAuthor();   // 리뷰 도서 저자
            String publisher = review.getBook().getPublisher(); // 리뷰 도서 출판사
            String coverImg = review.getBook().getCoverImg();   // 리뷰 도서 커버이미지
            Long isbn = review.getBook().getIsbn(); // 리뷰 도서 isbn

            // ReviewPostDetailResponseDto
            ReviewPostDetailResponseDto reviewPostDetailResponseDto = new ReviewPostDetailResponseDto();

            // ReviewPostDetailResponseDto 들을 담을 ArrayList -> ReviewPostDetailReviewCommentResponseDtos
            ArrayList<ReviewPostDetailReviewCommentResponseDto> ReviewPostDetailReviewCommentResponseDtos = new ArrayList<>();
            for (ReviewComment reviewComment : reviewComments) {
                // ReviewPostDetailReviewCommentResponseDtos 에 넣을 ReviewPostDetailReviewCommentResponseDto
                ReviewPostDetailReviewCommentResponseDto reviewPostDetailReviewCommentResponseDto = new ReviewPostDetailReviewCommentResponseDto();

                Long reviewCommentId = reviewComment.getId();
                String reviewCommentContent = reviewComment.getContent();
                LocalDateTime reviewCommentCreatedDate = reviewComment.getCreatedDate();
                String reviewCommentWriterNickname = reviewComment.getMember().getNickname();
                String reviewCommentWriterLoginId = reviewComment.getMember().getLoginId();
                Long reviewId = reviewComment.getReview().getId();

                reviewPostDetailReviewCommentResponseDto.setReviewCommentId(reviewCommentId);
                reviewPostDetailReviewCommentResponseDto.setContent(reviewCommentContent);
                reviewPostDetailReviewCommentResponseDto.setCreatedDate(reviewCommentCreatedDate);
                reviewPostDetailReviewCommentResponseDto.setNickname(reviewCommentWriterNickname);
                reviewPostDetailReviewCommentResponseDto.setLoginId(reviewCommentWriterLoginId);
                reviewPostDetailReviewCommentResponseDto.setReviewId(reviewId);

                ReviewPostDetailReviewCommentResponseDtos.add(reviewPostDetailReviewCommentResponseDto);
            }


            reviewPostDetailResponseDto.setId(id);
            reviewPostDetailResponseDto.setTitle(title);
            reviewPostDetailResponseDto.setContent(content);
            reviewPostDetailResponseDto.setView(nowView);
            reviewPostDetailResponseDto.setScore(score);
            reviewPostDetailResponseDto.setCreatedDate(createdDate);
            reviewPostDetailResponseDto.setNickname(writerNickname);
            reviewPostDetailResponseDto.setLoginId(writerLoginId);
            reviewPostDetailResponseDto.setReviewComments(ReviewPostDetailReviewCommentResponseDtos);
            reviewPostDetailResponseDto.setBookTitle(bookTitle);
            reviewPostDetailResponseDto.setAuthor(author);
            reviewPostDetailResponseDto.setPublisher(publisher);
            reviewPostDetailResponseDto.setCoverImg(coverImg);
            reviewPostDetailResponseDto.setIsbn(isbn);

            log.info("[리뷰 작성자의 로그인 아이디 : " + reviewPostWriterLoginId + ", 리뷰 글의 제목 : " + reviewPostWriterLoginId + "] 도서 리뷰글 상세 요청 성공");
            log.info("[END] - ReviewController.reviewPostDetail / 도서 리뷰글 상세 요청 완료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewPostDetailResponseDto);
        }
    }

    @GetMapping("/review/post/edit")
    public ResponseEntity<?> reviewPostEdit(Authentication authentication, @RequestParam(value = "loginId", required = true) String reviewPostWriterLoginId, @RequestParam(value = "title", required = true) String reviewPostTitle) {
        log.info("[START] - ReviewController.reviewPostEdit / 도서 리뷰 글 수정 데이터 접근 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // ReviewPostWriterLoginId 과 ReviewPostTitle 를 이용하여 해당하는 리뷰 글이 존재하는지 확인하기
        Review matchReviewPost = reviewService.findByLoginIdAndTitle(reviewPostWriterLoginId, reviewPostTitle);

        // 조건에 해당하는 리뷰 글이 존재한다면, 이 요청을 요청한 회원(JwtLoginId)이 해당 리뷰 글 작성자인지 확인(해당 글을 수정할 권한이 이 요청을 요청한 회원에게 있는지)
        if (matchReviewPost == null) {  // 조건에 해당하는 리뷰 글이 없다면, 수정 데이터 접근 거절

            log.info("요청 Body 의 조건에 해당하는 리뷰 글이 없음");
            log.info("도서 리뷰 글 수정 데이터 접근 실패");
            log.info("[END] - ReviewController.reviewPostEdit / 도서 리뷰 글 수정 데이터 접근 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰 글 수정 데이터 접근을 요청한 바디 조건에 해당하는 리뷰 글이 존재하지 않음");
        } else {    // 조건에 해당하는 리뷰 글이 존재한다면
            if (!(matchReviewPost.getMember().getLoginId().equals(JwtLoginId))) {     // 이 요청을 요청한 회원(JwtLoginId)이 해당 리뷰 글 작성자가 아님 (리뷰 글 수정 데이터 접근 권한 X)

                log.info("리뷰글 수정 데이터 접근을 요청한 회원 != 해당 리뷰 글 작성자");
                log.info("도서 리뷰 글 수정 데이터 접근 실패");
                log.info("[END] - ReviewController.reviewPostEdit / 도서 리뷰 글 수정 데이터 접근 요청 종료");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰 글 수정 데이터 접근을 요청한 회원은 해당 리뷰 글의 작성자가 아님");
            } else {    // 이 요청을 요청한 회원(JwtLoginId)이 해당 리뷰 글 작성자임 (리뷰 글 수정 데이터 접근 권한 O)
                // 해당 리뷰글의 상세 데이터를 반환
                String title = matchReviewPost.getTitle();       // 리뷰 글 제목
                String content = matchReviewPost.getContent();   // 리뷰 글 본문
                Long score = matchReviewPost.getScore();     // 리뷰 글 평점
                LocalDateTime createdDate = matchReviewPost.getCreatedDate();    // 리뷰 글 작성일
                String writerNickname = matchReviewPost.getMember().getNickname(); // 리뷰 글 작성자 닉네임

                String bookTitle = matchReviewPost.getBook().getTitle(); // 리뷰 도서 제목
                String author = matchReviewPost.getBook().getAuthor();   // 리뷰 도서 저자
                String publisher = matchReviewPost.getBook().getPublisher(); // 리뷰 도서 출판사
                String coverImg = matchReviewPost.getBook().getCoverImg();   // 리뷰 도서 커버이미지
                Long isbn = matchReviewPost.getBook().getIsbn(); // 리뷰 도서 isbn

                ReviewPostEditResponseDto reviewPostEditResponseDto = new ReviewPostEditResponseDto();
                reviewPostEditResponseDto.setTitle(title);
                reviewPostEditResponseDto.setContent(content);
                reviewPostEditResponseDto.setScore(score);
                reviewPostEditResponseDto.setCreatedDate(createdDate);
                reviewPostEditResponseDto.setNickname(writerNickname);
                reviewPostEditResponseDto.setBookTitle(bookTitle);
                reviewPostEditResponseDto.setAuthor(author);
                reviewPostEditResponseDto.setPublisher(publisher);
                reviewPostEditResponseDto.setCoverImg(coverImg);
                reviewPostEditResponseDto.setIsbn(isbn);

                log.info("도서 리뷰 글 수정 데이터 접근 성공");
                log.info("[END] - ReviewController.reviewPostEdit / 도서 리뷰 글 수정 데이터 접근 요청 종료");
                return ResponseEntity.status(HttpStatus.OK).body(reviewPostEditResponseDto);
            }
        }
    }

    @Transactional
    @PostMapping("/review/post/edit/save")
    public ResponseEntity<?> reviewPostEditSave(Authentication authentication, @RequestBody @Valid ReviewPostEditSaveRequestDto reviewPostEditSaveRequestDto) {
        log.info("[START] - ReviewController.reviewPostEditSave / 도서 리뷰 글 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 리뷰 글 제목, 작성자로 해당 리뷰 글을 DB 에서 찾고,
        // (이때 작성자 == JwtLoginId 이므로 이것을 이용하여 해당 리뷰글을 찾는 과정을 통해 (수정 요창자 == 해당 리뷰글 작성자) 임을 한번 더 검증 하는 방식을 취함)
        String title = reviewPostEditSaveRequestDto.getTitle();

        Review matchReview = reviewService.findByLoginIdAndTitle(JwtLoginId, title);

        if (matchReview == null) {      // 만약 해당 조건에 매치된 리뷰 글이 존재하지 않으면 수정 거절

            log.info("요청 조건에 맞는 리뷰 글이 존재하지 않음. 도서 리뷰 글 수정 저장 실패");
            log.info("[END] - ReviewController.reviewPostEditSave / 도서 리뷰 글 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 리뷰글에 대한 수정 권한이 없는 회원의 수정 요청임");
        } else {    // 만약 해당 조건에 매치된 리뷰 글이 존재하면 수정 진행
            // 수정이 허용된 필드
            String content = reviewPostEditSaveRequestDto.getContent(); // 리뷰 글의 본문
            Long score = reviewPostEditSaveRequestDto.getScore();       // 리뷰 글의 평점

            // 해당 리뷰 글에서 수정을 허용한 필드에 한해 각 필드에 set 으로 데이터를 수정해줌
            matchReview.editContent(content);
            matchReview.editScore(score);

            ReviewPostEditSaveResponseDto reviewPostEditSaveResponseDto = new ReviewPostEditSaveResponseDto();
            reviewPostEditSaveResponseDto.setLoginId(JwtLoginId);
            reviewPostEditSaveResponseDto.setTitle(matchReview.getTitle());

            log.info("작성자 아이디 : " + JwtLoginId + ", 리뷰 글 title : " + matchReview.getTitle() + "] 도서 리뷰 글 수정 저장 성공");
            log.info("[END] - ReviewController.reviewPostEditSave / 도서 리뷰 글 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(reviewPostEditSaveResponseDto);
        }
    }

    @GetMapping("/review/post/list")
    public Page<ReviewPostListResponseDto> reviewPostList(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - ReviewController.reviewPostList / 도서 리뷰 글 목록 요청 시작");

        Pageable pageable = PageRequest.of(page, size);
        log.info("[END] - ReviewController.reviewPostList / 도서 리뷰 글 목록 요청 종료");
        return reviewService.getReviewList(pageable)
                .map(review -> {
                    ReviewPostListResponseDto reviewPostListResponseDto = new ReviewPostListResponseDto();
                    reviewPostListResponseDto.setId(review.getId());
                    reviewPostListResponseDto.setTitle(review.getTitle());
                    reviewPostListResponseDto.setScore(review.getScore());
                    reviewPostListResponseDto.setCoverImg(review.getBook().getCoverImg());
                    reviewPostListResponseDto.setBookTitle(review.getBook().getTitle());
                    reviewPostListResponseDto.setAuthor(review.getBook().getAuthor());
                    reviewPostListResponseDto.setPublisher(review.getBook().getPublisher());
                    reviewPostListResponseDto.setNickname(review.getMember().getNickname());
                    reviewPostListResponseDto.setLoginId(review.getMember().getLoginId());
                    reviewPostListResponseDto.setCreatedDate(review.getCreatedDate());
                    reviewPostListResponseDto.setView(review.getView());
                    return reviewPostListResponseDto;
                });
    }

    @Transactional
    @PostMapping("/review/post/delete")
    public ResponseEntity<?> reviewPostDelete(Authentication authentication, @RequestBody @Valid ReviewPostDeleteRequestDto reviewPostDeleteRequestDto) {
        log.info("[START] - ReviewController.reviewPostDelete / 도서 리뷰 글 삭제 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // RequestBody 로 데이터 받기
        Long reviewId = reviewPostDeleteRequestDto.getId();   // 삭제를 요청하는 Review 의 id
        String reviewLoginId = reviewPostDeleteRequestDto.getLoginId(); // 삭제를 요청하는 Review 작성자의 loginId
        String reviewTitle = reviewPostDeleteRequestDto.getTitle(); // 삭제를 요청하는 Review 의 title
        List<Long> reviewCommentIds = reviewPostDeleteRequestDto.getReviewCommentIds(); // 삭제를 요청하는 Review 에 저장된 ReviewComment 들의 id

        // loginId 와 title 을 이용하여 Review 를 찾기
        Review matchReview = reviewService.findByLoginIdAndTitle(reviewLoginId, reviewTitle);
        if (matchReview == null) {  // 만약 loginId 와 title 로 Review 를 찾을 수 없다면 잘못된 요청임

            log.info("[END] - ReviewController.reviewPostDelete / 도서 리뷰 글 삭제 요청 종료");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요청 Body 의 loginId 와 title 로 매치되는 Review 가 없음");
        } else { // 매치된 해당 Review 의 id 가 RequestBody 의 id 와 일치하는지 검사
            Long matchReviewId = matchReview.getId();
            if (!(matchReviewId.equals(reviewId))) {    // 일치하지 않으면 잘못된 요청임

                log.info("[END] - ReviewController.reviewPostDelete / 도서 리뷰 글 삭제 요청 종료");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("loginId 와 title 로 매치되는 Review 는 찾았으나 id 가 매치되지 않음");
            } else {    // 일치하면 다음 로직 진행
                // 해당 회원이 삭제를 요청한 Review 의 작성자인지 검증
                if (!(reviewLoginId.equals(JwtLoginId))) {  // 해당 글 삭제에 대한 권한이 없음

                    log.info("[END] - ReviewController.reviewPostDelete / 도서 리뷰 글 삭제 요청 종료");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 Review 에 대한 삭제 권한이 없는 회원임");
                } else {    // 해당 Review 에 대한 삭제 권한이 있는 회원임
                    // 테이블 관계를 고려하여 Review 를 논리삭제 하기전에, 해당 Review 에 등록되어 있는 댓글이 있다면 해당 댓글들을 먼저 논리삭제 처리 해주어야함
                    // 해당 Review 에 등록된 ReviewComment 가 있는지 확인
                    if (reviewCommentIds != null) {     // 만약 등록된 댓글이 있다면 해당 댓글들을 논리삭제 진행
                        for (Long reviewCommentId : reviewCommentIds) {
                            log.info("[ReviewId : " + reviewId + "] 에 저장된 [ReviewCommentId : " + reviewCommentId + "] 논리 삭제 완료");
                            reviewCommentService.deleteReviewCommentById(reviewCommentId);
                        }
                    }

                    // 해당 Review 논리삭제 진행
                    reviewService.deleteReviewById(reviewId);

                    // 해당 Review 에 등록한 책 데이터 삭제 여부 검사
                    Long bookId = matchReview.getBook().getId();

                    // 해당 Review 를 삭제해도, 다른 Review 또는 Sell 에 등록된 책인 경우 아무것도 안함
                    // bookId 로 찾은 Sell
                    List<Sell> sellsByBookId = sellService.findSellsByBookId(bookId);
                    // bookId 로 찾은 Review
                    List<Review> reviewsByBookId = reviewService.findReviewsByBookId(bookId);

                    log.info(String.valueOf("sellsByBookId :" + sellsByBookId));
                    log.info(String.valueOf("reviewsByBookId : " + reviewsByBookId));

                    // 해당 Review 만 삭제하면 필요없어지는 책 데이터인 경우 -> 책 테이블에서 해당 책 데이터 논리 삭제
                    if ((sellsByBookId.size() == 0) & (reviewsByBookId.size() == 0)) {
                        log.info("[BookId : " + bookId + "] 데이터가 더이상 필요하지 않아서 논리 삭제");
                        bookService.deleteBookById(bookId);
                    }

                    ReviewPostDeleteResponseDto reviewPostDeleteResponseDto = new ReviewPostDeleteResponseDto();
                    reviewPostDeleteResponseDto.setLoginId(JwtLoginId);

                    log.info("[END] - ReviewController.reviewPostDelete / 도서 리뷰 글 삭제 요청 종료");
                    return ResponseEntity.status(HttpStatus.OK).body(reviewPostDeleteResponseDto);
                }
            }
        }
    }

    @GetMapping("/review/post/list/search/by/title")
    public Page<ReviewListSearchByTitleResponseDto> reviewListSearchByTitle(@RequestParam(value = "keyword") String title, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - ReviewController.reviewListSearchByTitle / 도서 리뷰 목록을 제목으로 검색 요청 시작");

        Pageable pageable = PageRequest.of(page, size);

        log.info("[END] - ReviewController.reviewListSearchByTitle / 도서 리뷰 목록을 제목으로 검색 요청 종료");
        return reviewService.searchReviewsByTitleKeywords(title, pageable)
                .map(review -> {
                    ReviewListSearchByTitleResponseDto reviewListSearchByTitleResponseDto = new ReviewListSearchByTitleResponseDto();
                    reviewListSearchByTitleResponseDto.setId(review.getId());
                    reviewListSearchByTitleResponseDto.setTitle(review.getTitle());
                    reviewListSearchByTitleResponseDto.setScore(review.getScore());
                    reviewListSearchByTitleResponseDto.setCoverImg(review.getBook().getCoverImg());
                    reviewListSearchByTitleResponseDto.setBookTitle(review.getBook().getTitle());
                    reviewListSearchByTitleResponseDto.setAuthor(review.getBook().getAuthor());
                    reviewListSearchByTitleResponseDto.setPublisher(review.getBook().getPublisher());
                    reviewListSearchByTitleResponseDto.setNickname(review.getMember().getNickname());
                    reviewListSearchByTitleResponseDto.setLoginId(review.getMember().getLoginId());
                    reviewListSearchByTitleResponseDto.setCreatedDate(review.getCreatedDate());
                    reviewListSearchByTitleResponseDto.setView(review.getView());
                    return reviewListSearchByTitleResponseDto;
                });
    }

    @GetMapping("/review/post/list/search/by/booktitle")
    public Page<ReviewListSearchByBookTitleResponseDto> reviewListSearchByBookTitle(@RequestParam(value = "keyword") String title, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - ReviewController.reviewListSearchByTitle / 도서 리뷰 목록을 제목으로 검색 요청 시작");

        Pageable pageable = PageRequest.of(page, size);

        log.info("[END] - ReviewController.reviewListSearchByTitle / 도서 리뷰 목록을 제목으로 검색 요청 종료");
        return reviewService.searchReviewsByBookTitle(title, pageable)
                .map(review -> {
                    ReviewListSearchByBookTitleResponseDto reviewListSearchByBookTitleResponseDto = new ReviewListSearchByBookTitleResponseDto();
                    reviewListSearchByBookTitleResponseDto.setId(review.getId());
                    reviewListSearchByBookTitleResponseDto.setTitle(review.getTitle());
                    reviewListSearchByBookTitleResponseDto.setScore(review.getScore());
                    reviewListSearchByBookTitleResponseDto.setCoverImg(review.getBook().getCoverImg());
                    reviewListSearchByBookTitleResponseDto.setBookTitle(review.getBook().getTitle());
                    reviewListSearchByBookTitleResponseDto.setAuthor(review.getBook().getAuthor());
                    reviewListSearchByBookTitleResponseDto.setPublisher(review.getBook().getPublisher());
                    reviewListSearchByBookTitleResponseDto.setNickname(review.getMember().getNickname());
                    reviewListSearchByBookTitleResponseDto.setLoginId(review.getMember().getLoginId());
                    reviewListSearchByBookTitleResponseDto.setCreatedDate(review.getCreatedDate());
                    reviewListSearchByBookTitleResponseDto.setView(review.getView());
                    return reviewListSearchByBookTitleResponseDto;
                });
    }

    @GetMapping("/review/post/list/search/by/author")
    public Page<ReviewListSearchByAuthorResponseDto> reviewListSearchByAuthor(@RequestParam(value = "keyword") String title, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - ReviewController.reviewListSearchByAuthor / 도서 리뷰 목록을 저자로 검색 요청 시작");

        Pageable pageable = PageRequest.of(page, size);

        log.info("[END] - ReviewController.reviewListSearchByAuthor / 도서 리뷰 목록을 저자로 검색 요청 종료");
        return reviewService.searchReviewsByAuthorKeywords(title, pageable)
                .map(review -> {
                    ReviewListSearchByAuthorResponseDto reviewListSearchByAuthorResponseDto = new ReviewListSearchByAuthorResponseDto();
                    reviewListSearchByAuthorResponseDto.setId(review.getId());
                    reviewListSearchByAuthorResponseDto.setTitle(review.getTitle());
                    reviewListSearchByAuthorResponseDto.setScore(review.getScore());
                    reviewListSearchByAuthorResponseDto.setCoverImg(review.getBook().getCoverImg());
                    reviewListSearchByAuthorResponseDto.setBookTitle(review.getBook().getTitle());
                    reviewListSearchByAuthorResponseDto.setAuthor(review.getBook().getAuthor());
                    reviewListSearchByAuthorResponseDto.setPublisher(review.getBook().getPublisher());
                    reviewListSearchByAuthorResponseDto.setNickname(review.getMember().getNickname());
                    reviewListSearchByAuthorResponseDto.setLoginId(review.getMember().getLoginId());
                    reviewListSearchByAuthorResponseDto.setCreatedDate(review.getCreatedDate());
                    reviewListSearchByAuthorResponseDto.setView(review.getView());
                    return reviewListSearchByAuthorResponseDto;
                });
    }

    @GetMapping("/review/post/list/search/by/nickname")
    public Page<ReviewListSearchByNicknameResponseDto> reviewListSearchByNickname(@RequestParam(value = "keyword") String title, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - ReviewController.reviewListSearchByNickname / 도서 리뷰 목록을 작성자닉네임으로 검색 요청 시작");

        Pageable pageable = PageRequest.of(page, size);

        log.info("[END] - ReviewController.reviewListSearchByNickname / 도서 리뷰 목록을 작성자닉네임으로 검색 요청 종료");
        return reviewService.searchReviewsByMemberNicknameKeywords(title, pageable)
                .map(review -> {
                    ReviewListSearchByNicknameResponseDto reviewListSearchByNicknameResponseDto = new ReviewListSearchByNicknameResponseDto();
                    reviewListSearchByNicknameResponseDto.setId(review.getId());
                    reviewListSearchByNicknameResponseDto.setTitle(review.getTitle());
                    reviewListSearchByNicknameResponseDto.setScore(review.getScore());
                    reviewListSearchByNicknameResponseDto.setCoverImg(review.getBook().getCoverImg());
                    reviewListSearchByNicknameResponseDto.setBookTitle(review.getBook().getTitle());
                    reviewListSearchByNicknameResponseDto.setAuthor(review.getBook().getAuthor());
                    reviewListSearchByNicknameResponseDto.setPublisher(review.getBook().getPublisher());
                    reviewListSearchByNicknameResponseDto.setNickname(review.getMember().getNickname());
                    reviewListSearchByNicknameResponseDto.setLoginId(review.getMember().getLoginId());
                    reviewListSearchByNicknameResponseDto.setCreatedDate(review.getCreatedDate());
                    reviewListSearchByNicknameResponseDto.setView(review.getView());
                    return reviewListSearchByNicknameResponseDto;
                });
    }
}
