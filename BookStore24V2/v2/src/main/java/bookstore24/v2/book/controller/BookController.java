package bookstore24.v2.book.controller;

import bookstore24.v2.book.dto.*;
import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;
    private final MemberService memberService;

    @Value(("${spring.security.oauth2.client.registration.naver.client-id}"))
    private String clientId;

    @Value(("${spring.security.oauth2.client.registration.naver.client-secret}"))
    private String clientSecret;

    final String NAVER_BOOK_INFORMATION_REQUEST = "https://openapi.naver.com/v1/search/book.json?display=10&query=";     // 네이버 책 정보 검색 API(JSON)


    @GetMapping("/book/information/search")
    public ResponseEntity<?> bookInformationSearch(Authentication authentication, @RequestParam(value = "query", required = true) String query) {
        log.info("[START] - BookController.bookInformationSearch / 네이버 api 에 책 정보를 요청을 시작");

        if (query.equals("")) {

            log.info("검색어 없음 -> 책 정보 반환 실패");
            log.info("[END] - BookController.bookInformationSearch / 네이버 api 에 책 정보를 요청을 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("검색어를 입력하지 않음");
        }

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // GET 으로 네이버 책 정보 검색 API 에 요청
        // 사용 라이브러리 - RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Naver-Client-Id", clientId);
        httpHeaders.set("X-Naver-Client-Secret", clientSecret);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<String> bookInformationSearchRequest = new HttpEntity<>(httpHeaders);
        ResponseEntity<NaverBookSearchApiResponseDto> response = restTemplate.exchange(
                NAVER_BOOK_INFORMATION_REQUEST + query,  // 네이버 책 정보 API 요청 주소
                HttpMethod.GET,    // 요청 메서드는 네이버 API 문서상의 GET
                bookInformationSearchRequest,   // HttpEntity 에 요청에 필요한 정보를 한번에 넣어줌
                NaverBookSearchApiResponseDto.class);   // 응답받을 타입

        NaverBookSearchApiResponseDto naverBookSearchApiResponseDto = response.getBody();

        List<BookInformationSearchResponseDto> bookItems = naverBookSearchApiResponseDto.getItems();

        log.info("검색어 : " + query + " 에 대한 책 정보 반환 성공");
        log.info("[END] - BookController.bookInformationSearch / 네이버 api 에 책 정보를 요청을 종료");
        return ResponseEntity.status(HttpStatus.OK).body(bookItems);
    }

    @GetMapping("/book/ranking/score")
    public ResponseEntity<?> bookRankingScore() {
        log.info("[START] - BookController.bookRankingScore / 도서 평점 랭킹 요청 시작");

        List<Book> allBooksOrderByAverageScoreDesc = bookService.findTop10BooksOrderByAverageScoreDesc();

        // BookRankingScoreResponseDto
        BookRankingScoreResponseDto bookRankingScoreResponseDto = new BookRankingScoreResponseDto();

        // BookRankingScoreBookResponseDto 들을 담을 ArrayList -> BookRankingScoreBookResponseDtos
        ArrayList<BookRankingScoreBookResponseDto> bookRankingScoreBookResponseDtos = new ArrayList<>();

        for (Book book : allBooksOrderByAverageScoreDesc) {
            BookRankingScoreBookResponseDto bookRankingScoreBookResponseDto = new BookRankingScoreBookResponseDto();

            Long id = book.getId();
            String title = book.getTitle();
            String author = book.getAuthor();
            String publisher = book.getPublisher();
            Double avgScore = book.getAvgScore();
            String coverImg = book.getCoverImg();
            Long isbn = book.getIsbn();

            bookRankingScoreBookResponseDto.setId(id);
            bookRankingScoreBookResponseDto.setTitle(title);
            bookRankingScoreBookResponseDto.setAuthor(author);
            bookRankingScoreBookResponseDto.setPublisher(publisher);
            bookRankingScoreBookResponseDto.setAvgScore(avgScore);
            bookRankingScoreBookResponseDto.setCoverImg(coverImg);
            bookRankingScoreBookResponseDto.setIsbn(isbn);

            bookRankingScoreBookResponseDtos.add(bookRankingScoreBookResponseDto);
        }
        bookRankingScoreResponseDto.setBooks(bookRankingScoreBookResponseDtos);

        log.info("[END] - BookController.bookRankingScore / 도서 평점 랭킹 요청 종료");
        return ResponseEntity.status(HttpStatus.OK).body(bookRankingScoreResponseDto);
    }

    @GetMapping("/book/ranking/view/review")
    public ResponseEntity<?> bookRankingViewReview() {
        log.info("[START] - BookController.bookRankingViewReview / 도서 리뷰 조회수 랭킹 요청 시작");

        List<Book> allBooksOrderByTotalReviewViewDesc = bookService.findTop10BooksOrderByTotalReviewViewDesc();

        // BookRankingViewReviewResponseDto
        BookRankingViewReviewResponseDto bookRankingViewReviewResponseDto = new BookRankingViewReviewResponseDto();

        // BookRankingViewReviewBookResponseDto 들을 담을 ArrayList -> BookRankingViewReviewBookResponseDtos
        ArrayList<BookRankingViewReviewBookResponseDto> bookRankingViewReviewBookResponseDtos = new ArrayList<>();

        for (Book book : allBooksOrderByTotalReviewViewDesc) {
            BookRankingViewReviewBookResponseDto bookRankingViewReviewBookResponseDto = new BookRankingViewReviewBookResponseDto();

            Long id = book.getId();
            String title = book.getTitle();
            String author = book.getAuthor();
            String publisher = book.getPublisher();
            Double avgScore = book.getAvgScore();
            String coverImg = book.getCoverImg();
            Long isbn = book.getIsbn();

            bookRankingViewReviewBookResponseDto.setId(id);
            bookRankingViewReviewBookResponseDto.setTitle(title);
            bookRankingViewReviewBookResponseDto.setAuthor(author);
            bookRankingViewReviewBookResponseDto.setPublisher(publisher);
            bookRankingViewReviewBookResponseDto.setAvgScore(avgScore);
            bookRankingViewReviewBookResponseDto.setCoverImg(coverImg);
            bookRankingViewReviewBookResponseDto.setIsbn(isbn);

            bookRankingViewReviewBookResponseDtos.add(bookRankingViewReviewBookResponseDto);
        }
        bookRankingViewReviewResponseDto.setBooks(bookRankingViewReviewBookResponseDtos);

        log.info("[END] - BookController.bookRankingViewReview / 도서 리뷰 조회수 랭킹 요청 종료");
        return ResponseEntity.status(HttpStatus.OK).body(bookRankingViewReviewResponseDto);
    }
}
