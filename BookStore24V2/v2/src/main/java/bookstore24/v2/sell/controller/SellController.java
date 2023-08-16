package bookstore24.v2.sell.controller;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.domain.SellStatus;
import bookstore24.v2.member.service.MemberService;
import bookstore24.v2.sell.dto.*;
import bookstore24.v2.sell.service.SellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

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
        } else if ((existStatusBook == null) & (duplicateTitleSell == null)) {    // 데이터베이스에 해당 책을 추가 등록)
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

    @Transactional
    @GetMapping("/sell/post/detail")
    public ResponseEntity<?> sellPostDetail(Authentication authentication, @RequestParam(value = "loginId", required = true) String sellPostWriterLoginId, @RequestParam(value = "title", required = true) String sellPostTitle) {
        log.info("[START] - SellController.sellPostDetail / 도서 판매글 상세 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 판매글 작성자의 loginId, 판매글의 제목 title 을 이용하여 해당하는 Sell 글 찾기
        Sell sell = sellService.findByLoginIdAndTitle(sellPostWriterLoginId, sellPostTitle);
        log.info("로그인 아이디와 타이틀로 찾은 판매 : " + sell);

        // 판매글 상세 데이터 반환하기
        if (sell == null) {   // sellLoginId, sellTitle 으로 해당하는 판매 글이 존재하지 않음.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 조건의 판매 글이 존재하지 않음");
        } else {
            // sellLoginId, sellTitle 으로 해당하는 판매 글의 조회수를 상세 글 데이터를 요청할 때마다 +1 해줌
            Long view = sell.getView();
            if (view == null) { // 만약 해당 판매 글의 상세를 최초로 조회하는 것이라면,
                log.info("[판매 작성자의 로그인 아이디 : " + sellPostWriterLoginId + ", 판매 글의 제목 : " + sellPostTitle + "] 도서 판매글 상세가 최초로 요청됨. 조회수 0으로 초기화 완료");
                sell.initView();  // 해당 판매 글의 view 를 0 으로 초기화
            }
            Long inquiryView = sell.getView();    // 판매 글 상세를 조회하기 전의 view
            sell.setView(inquiryView);            // 판매 글 상세를 조회 -> (판매 글 상세를 조회하기 전의 view)  + 1
            log.info("[판매글 작성자의 로그인 아이디 : " + sellPostWriterLoginId + ", 판매 글의 제목 : " + sellPostTitle + "] 판매 글 조회수 : " + sell.getView() + " 로 업데이트 완료");

            // 해당 판매글의 상세 데이터를 반환
            Long id = sell.getId(); // 판매 글 아이디
            String title = sell.getTitle();// 판매 글 제목
            String content = sell.getContent();// 판매 글 본문
            Long nowView = sell.getView();// 판매 글 조회수
            LocalDateTime createdDate = sell.getCreatedDate();// 판매 글 작성일
            String writerNickname = sell.getMember().getNickname();// 판매글 작성자 닉네임
            String writerLoginId = sell.getMember().getLoginId();   // 판매글 작성자 로그인 아이디
            Long price = sell.getPrice();// 판매 글 가격
            String talkUrl = sell.getTalkUrl();// 판매 글 채팅 url
            SellStatus status = sell.getStatus();// 판매 글 상태

            String bookTitle = sell.getBook().getTitle();// 판매 도서 제목
            String author = sell.getBook().getAuthor();// 판매 도서 저자
            String publisher = sell.getBook().getPublisher();// 판매 도서 출판사
            String coverImg = sell.getBook().getCoverImg();// 판매 도서 커버이미지
            Long isbn = sell.getBook().getIsbn();// 판매 도서 isbn

            SellPostDetailResponseDto sellPostDetailResponseDto = new SellPostDetailResponseDto();
            sellPostDetailResponseDto.setId(id);
            sellPostDetailResponseDto.setTitle(title);
            sellPostDetailResponseDto.setContent(content);
            sellPostDetailResponseDto.setView(nowView);
            sellPostDetailResponseDto.setCreatedDate(createdDate);
            sellPostDetailResponseDto.setNickname(writerNickname);
            sellPostDetailResponseDto.setLoginId(writerLoginId);
            sellPostDetailResponseDto.setPrice(price);
            sellPostDetailResponseDto.setTalkUrl(talkUrl);
            sellPostDetailResponseDto.setStatus(status);
            sellPostDetailResponseDto.setBookTitle(bookTitle);
            sellPostDetailResponseDto.setAuthor(author);
            sellPostDetailResponseDto.setPublisher(publisher);
            sellPostDetailResponseDto.setCoverImg(coverImg);
            sellPostDetailResponseDto.setIsbn(isbn);

            log.info("[판매 작성자의 로그인 아이디 : " + sellPostWriterLoginId + ", 판매 글의 제목 : " + sellPostTitle + "] 도서 판매글 상세 요청 성공");
            log.info("[END] - SellController.sellPostDetail / 도서 판매글 상세 요청 완료");
            return ResponseEntity.status(HttpStatus.OK).body(sellPostDetailResponseDto);
        }
    }

    @GetMapping("/sell/post/edit")
    public ResponseEntity<?> sellPostEdit(Authentication authentication, @RequestParam(value = "loginId", required = true) String sellPostWriterLoginId, @RequestParam(value = "title", required = true) String sellPostTitle) {
        log.info("[START] - SellController.sellPostEdit / 도서 판매글 수정 데이터 접근 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // SellPostWriterLoginId 과 SellPostTitle 를 이용하여 해당하는 판매 글이 존재하는지 확인하기
        Sell matchSellPost = sellService.findByLoginIdAndTitle(sellPostWriterLoginId, sellPostTitle);

        // 조건에 해당하는 판매 글이 존재한다면, 이 요청을 요청한 회원(JwtLoginId)이 해당 판매 글 작성자인지 확인(해당 글을 수정할 권한이 이 요청을 요청한 회원에게 있는지)
        if (matchSellPost == null) {  // 조건에 해당하는 판매 글이 없다면, 수정 데이터 접근 거절
            log.info("요청 Body 의 조건에 해당하는 판매 글이 없음");
            log.info("도서 판매 글 수정 데이터 접근 실패");
            log.info("[END] - SellController.sellPostEdit / 도서 판매글 수정 데이터 접근 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("판매 글 수정 데이터 접근을 요청한 바디 조건에 해당하는 판매 글이 존재하지 않음");
        } else {
            if (!(matchSellPost.getMember().getLoginId().equals(JwtLoginId))) {     // 이 요청을 요청한 회원(JwtLoginId)이 해당 판매 글 작성자가 아님 (판매 글 수정 데이터 접근 권한 X)
                log.info("판매 글 수정 데이터 접근을 요청한 회원 != 해당 판매 글 작성자");
                log.info("도서 판매 글 수정 데이터 접근 실패");
                log.info("[END] - SellController.sellPostEdit / 도서 판매글 수정 데이터 접근 요청 종료");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("판매 글 수정 데이터 접근을 요청한 회원은 해당 판매 글의 작성자가 아님");
            } else {    // 이 요청을 요청한 회원(JwtLoginId)이 해당 판매 글 작성자임 (판매 글 수정 데이터 접근 권한 O)
                // 해당 판매글의 데이터를 반환
                String title = matchSellPost.getTitle();
                String bookTitle = matchSellPost.getBook().getTitle();
                String author = matchSellPost.getBook().getAuthor();
                String publisher = matchSellPost.getBook().getPublisher();
                String coverImg = matchSellPost.getBook().getCoverImg();
                Long isbn = matchSellPost.getBook().getIsbn();
                String content = matchSellPost.getContent();
                Long price = matchSellPost.getPrice();
                String talkUrl = matchSellPost.getTalkUrl();
                LocalDateTime createdDate = matchSellPost.getCreatedDate();
                String writerNickname = matchSellPost.getMember().getNickname();

                SellPostEditResponseDto sellPostEditResponseDto = new SellPostEditResponseDto();
                sellPostEditResponseDto.setTitle(title);
                sellPostEditResponseDto.setBookTitle(bookTitle);
                sellPostEditResponseDto.setAuthor(author);
                sellPostEditResponseDto.setPublisher(publisher);
                sellPostEditResponseDto.setCoverImg(coverImg);
                sellPostEditResponseDto.setIsbn(isbn);
                sellPostEditResponseDto.setContent(content);
                sellPostEditResponseDto.setPrice(price);
                sellPostEditResponseDto.setTalkUrl(talkUrl);
                sellPostEditResponseDto.setCreatedDate(createdDate);
                sellPostEditResponseDto.setNickname(writerNickname);

                log.info("도서 판매 글 수정 데이터 접근 성공");
                log.info("[END] - SellController.sellPostEdit / 도서 판매글 수정 데이터 접근 요청 종료");
                return ResponseEntity.status(HttpStatus.OK).body(sellPostEditResponseDto);
            }
        }
    }

    @Transactional
    @PostMapping("/sell/post/edit/save")
    public ResponseEntity<?> sellPostEditSave(Authentication authentication, @RequestBody @Valid SellPostEditSaveRequestDto sellPostEditSaveRequestDto) {
        log.info("[START] - SellController.sellPostEditSave / 도서 판매글 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 판매 글 제목, 작성자로 해당 판매 글을 DB 에서 찾고,
        // (이때 작성자 == JwtLoginId 이므로 이것을 이용하여 해당 판매글을 찾는 과정을 통해 (수정 요창자 == 해당 판매글 작성자) 임을 한번 더 검증 하는 방식을 취함)
        String title = sellPostEditSaveRequestDto.getTitle();

        Sell matchSell = sellService.findByLoginIdAndTitle(JwtLoginId, title);

        if (matchSell == null) {    // 만약 해당 조건에 매치된 판매 글이 존재하지 않으면 수정 거

            log.info("요청 조건에 맞는 판매 글이 존재하지 않음. 도서 판매 글 수정 저장 실패");
            log.info("[END] - SellController.sellPostEditSave / 도서 판매 글 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 판매글에 대한 수정 권한이 없는 회원의 수정 요청임");
        } else {    // 만약 해당 조건에 매치된 판매 글이 존재하면 수정 진행
            // 수정이 허용된 필드
            String content = sellPostEditSaveRequestDto.getContent(); // 판매 글의 본문
            Long price = sellPostEditSaveRequestDto.getPrice();       // 판매 글의 가격
            String talkUrl = sellPostEditSaveRequestDto.getTalkUrl();       // 판매 글의 채팅

            // 해당 판매 글에서 수정을 허용한 필드에 한해 각 필드에 set 으로 데이터를 수정해줌
            matchSell.editContent(content);
            matchSell.editPrice(price);
            matchSell.editTalkUrl(talkUrl);

            SellPostEditSaveResponseDto sellPostEditSaveResponseDto = new SellPostEditSaveResponseDto();
            sellPostEditSaveResponseDto.setLoginId(JwtLoginId);
            sellPostEditSaveResponseDto.setTitle(matchSell.getTitle());

            log.info("작성자 아이디 : " + JwtLoginId + ", 판매 글 title : " + matchSell.getTitle() + "] 도서 판매 글 수정 저장 성공");
            log.info("[END] - SellController.sellPostEditSave / 도서 판매 글 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(sellPostEditSaveResponseDto);
        }
    }

    @Transactional
    @PostMapping("/sell/post/status/edit/save")
    public ResponseEntity<?> sellPostStatusEditSave(Authentication authentication, @RequestBody @Valid SellPostStatusEditSaveRequestDto sellPostStatusEditSaveRequestDto) {
        log.info("[START] - SellController.sellPostStatusEditSave / 도서 판매글 상태 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 상태 변경을 요청한 판매 글의 작성자의 로그인 아이디와 제목
        String sellPostWriterLoginId = sellPostStatusEditSaveRequestDto.getLoginId();
        String sellPostTitle = sellPostStatusEditSaveRequestDto.getTitle();

        // 상태 변경을 요청한 판매 글 찾기
        Sell matchSellPost = sellService.findByLoginIdAndTitle(sellPostWriterLoginId, sellPostTitle);

        if (matchSellPost == null) {    // 상태 변경을 요청한 판매 글이 존재하지 않으면

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("판매 상태 수정 요청 조건에 해당하는 판매 글이 존재하지 않음");
        } else {    // 상태 변경을 요청한 판매 글이 존재하면
            // 상태 수정을 요청한 회원과 상태 수정을 요청한 글의 작성자가 동일인인지 확인
            if (!(JwtLoginId.equals(sellPostWriterLoginId))) {  // 만약 동일인이 아니면 수정권한 없음
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 유저는 해당 판매 글의 상태 수정 권한이 없음");
            } else {    // 동일인이면 수정권한 있음.
                // 판매글의 현재 상태
                SellStatus status = matchSellPost.getStatus();
                if (status.equals(SellStatus.on)) {  // 기존의 판매 상태가 on 이면
                    log.info("기존의 판매 상태가 on 이어서 off 로 변경");
                    matchSellPost.editSellStatusToOff();    // 판매 상태를 off 로 변경
                    SellPostStatusEditSaveResponseDto sellPostStatusEditSaveResponseDto = new SellPostStatusEditSaveResponseDto();
                    sellPostStatusEditSaveResponseDto.setLoginId(sellPostWriterLoginId);
                    sellPostStatusEditSaveResponseDto.setTitle(sellPostTitle);
                    return ResponseEntity.status(HttpStatus.OK).body(sellPostStatusEditSaveResponseDto);
                } else {    // 기존의 판매 상태가 off 이면
                    log.info("기존의 판매 상태가 off 이어서 on 로 변경");
                    matchSellPost.editSellStatusToOn();     // 판매 상태를 on 으로 변경
                    SellPostStatusEditSaveResponseDto sellPostStatusEditSaveResponseDto = new SellPostStatusEditSaveResponseDto();
                    sellPostStatusEditSaveResponseDto.setLoginId(sellPostWriterLoginId);
                    sellPostStatusEditSaveResponseDto.setTitle(sellPostTitle);
                    return ResponseEntity.status(HttpStatus.OK).body(sellPostStatusEditSaveResponseDto);
                }
            }
        }
    }
}


