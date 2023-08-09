package bookstore24.v2.sell.controller;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.domain.SellStatus;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.sell.dto.SellPostSaveRequestDto;
import bookstore24.v2.sell.dto.SellPostSaveResponseDto;
import bookstore24.v2.sell.service.SellService;
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
public class SellController {

    private final MemberService memberService;
    private final BookService bookService;
    private final SellService sellService;


    @Transactional
    @PostMapping("/sell/post/save")
    public ResponseEntity<?> sellPostSave(Authentication authentication, @RequestBody @Valid SellPostSaveRequestDto sellPostSaveRequestDto) {
        log.info("[START] - SellController.sellPostSave / 도서 판매글 작성 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        String title = sellPostSaveRequestDto.getTitle();
        String bookTitle = sellPostSaveRequestDto.getBookTitle();
        String author = sellPostSaveRequestDto.getAuthor();
        String publisher = sellPostSaveRequestDto.getPublisher();
        String coverImg = sellPostSaveRequestDto.getCoverImg();
        Long isbn = sellPostSaveRequestDto.getIsbn();
        String talkUrl = sellPostSaveRequestDto.getTalkUrl();
        Long price = sellPostSaveRequestDto.getPrice();
        String content = sellPostSaveRequestDto.getContent();

        // 데이터베이스에 해당 도서가 저장되어 있는지 isbn 으로 조회
        Book existStatusBook = bookService.findByIsbn(isbn);

        // 같은 제목으로 등록된 판매 글이 있는지 확인
        Sell duplicateTitleSell = sellService.duplicateTitleSell(title);

        if ((existStatusBook != null) & (duplicateTitleSell == null)) {  // 데이터베이스에 해당 책을 추가로 등록하지는 않음
            // 도서 판매글 생성 및 저장
            Sell sell = new Sell(title, content, price, SellStatus.on, talkUrl);
            Sell savedSell = sellService.saveSell(sell, member, existStatusBook);

            // SellPostSaveResponseDto 에 판매글 작성자의 loginId 입력
            SellPostSaveResponseDto sellPostSaveResponseDto = new SellPostSaveResponseDto();
            sellPostSaveResponseDto.setLoginId(JwtLoginId);

            // SellPostSaveResponseDto 에 판매글의 id 입력
            Long savedSellId = savedSell.getId();
            sellPostSaveResponseDto.setId(savedSellId);

            // SellPostSaveResponseDto 에 판매글의 제목 입력
            sellPostSaveResponseDto.setTitle(title);

            log.info("[도서명 : " + bookTitle + "] 는 DB에 저장되어 있으므로 판매글만 저장 완료");
            log.info("[END] - SellController.sellPostSave / 도서 판매글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(sellPostSaveResponseDto);
        } else if((existStatusBook == null) & (duplicateTitleSell == null)) {    // 데이터베이스에 해당 책을 추가 등록)
            // 데이터베이스에 해당 책 저장
            Book book = new Book(isbn, bookTitle, author, publisher, coverImg);
            Book savedBook = bookService.saveBook(book);

            // 도서 판매글 생성 및 저장
            Sell sell = new Sell(title, content, price, SellStatus.on, talkUrl);
            Sell savedSell = sellService.saveSell(sell, member, savedBook);

            // SellPostSaveResponseDto 에 판매 글 작성자의 loginId 입력
            SellPostSaveResponseDto sellPostSaveResponseDto = new SellPostSaveResponseDto();
            sellPostSaveResponseDto.setLoginId(JwtLoginId);

            // SellPostSaveResponseDto 에 판매 글의 id 입력
            Long savedSellId = savedSell.getId();
            sellPostSaveResponseDto.setId(savedSellId);

            // SellPostSaveResponseDto 에 판매 글의 제목 입력
            sellPostSaveResponseDto.setTitle(title);

            log.info("[도서명 : " + bookTitle + "] 는 DB에 저장되어 있지 않으므로 도서 + 판매 글 함께 저장 완료");
            log.info("[END] - SellController.sellPostSave / 도서 판매글 작성 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(sellPostSaveResponseDto);
        }
        log.info("판매 글 제목 중복으로 저장 실패");
        log.info("[END] - SellController.sellPostSave / 도서 판매글 작성 저장 요청 종료");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("판매 글 제목 중복으로 저장 실패");
    }
}
