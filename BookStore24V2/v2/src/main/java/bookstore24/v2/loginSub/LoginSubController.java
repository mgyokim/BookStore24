package bookstore24.v2.loginSub;

import bookstore24.v2.auth.oauth.dto.token.GoogleOauthTokenDto;
import bookstore24.v2.auth.oauth.dto.token.KakaoOauthTokenDto;
import bookstore24.v2.auth.oauth.dto.token.NaverOauthTokenDto;
import bookstore24.v2.book.repository.BookRepository;
import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Review;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.loginSub.dto.BookListSubResponseDto;
import bookstore24.v2.loginSub.dto.MemberListSubResponseDto;
import bookstore24.v2.loginSub.dto.ReviewListSubResponseDto;
import bookstore24.v2.loginSub.dto.SellListSubResponseDto;
import bookstore24.v2.member.repository.MemberRepository;
import bookstore24.v2.review.repository.ReviewRepository;
import bookstore24.v2.sell.repository.SellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginSubController {

    private final KakaoLogicSub kakaoLogicSub;
    private final NaverLogicSub naverLogicSub;
    private final GoogleLogicSub googleLogicSub;

    private final MemberRepository memberRepository;

    private final BookRepository bookRepository;

    private final ReviewRepository reviewRepository;

    private final SellRepository sellRepository;

    /**
     * 개발용 테스트용 컨트롤러
     * [카카오 로컬개발용]
     * GET
     * - local:8080
     * https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=e435f34295d28879dfabc32de2bd7546&redirect_uri=http://localhost:8080/auth/kakao/callback
     * - AWS EC2
     * https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=e435f34295d28879dfabc32de2bd7546&redirect_uri=http://bookstore24.shop/auth/kakao/callback
     *
     * [네이버 로컬 개발용]
     * GET
     * - local:8080
     * https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://localhost:8080/auth/naver/callback&state='test'
     *  - AWS EC2
     * https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://bookstore24.shop/auth/naver/callback&state='test'
     *
     *
     * [구글 로컬 개발용]
     * GET
     * - local:8080
     * https://accounts.google.com/o/oauth2/v2/auth?client_id=766446517759-t82jo5h4vk9rmj30bld1d30su7sqdde1.apps.googleusercontent.com&redirect_uri=http://localhost:8080/auth/google/callback&response_type=code&scope=openid%20email%20profile
     * - AWS EC2
     * https://accounts.google.com/o/oauth2/v2/auth?client_id=766446517759-t82jo5h4vk9rmj30bld1d30su7sqdde1.apps.googleusercontent.com&redirect_uri=http://bookstore24.shop/auth/google/callback&response_type=code&scope=openid%20email%20profile
     */

    @GetMapping("/auth/kakao/callback")
    ResponseEntity<String> kakaoLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthTokenDto kakaoOauthTokenDto = kakaoLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogicSub.accessTokenToProfile(kakaoOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = kakaoLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = kakaoLogicSub.kakaoAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("kakaoLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = kakaoLogicSub.kakaoAutoLoginFail(email, provider);

            log.info("kakaoLoginSub 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @GetMapping("/auth/naver/callback")
    ResponseEntity<String> naverLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        NaverOauthTokenDto naverOauthTokenDto = naverLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = naverLogicSub.accessTokenToProfile(naverOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = naverLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = naverLogicSub.naverAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("naverLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = naverLogicSub.naverAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @GetMapping("/auth/google/callback")
    ResponseEntity<String> googleLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        GoogleOauthTokenDto googleOauthTokenDto = googleLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = googleLogicSub.accessTokenToProfile(googleOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = googleLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = googleLogicSub.googleAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("googleLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = googleLogicSub.googleAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @GetMapping("/member/list/sub")
    public List<MemberListSubResponseDto> memberListSub() {
        List<Member> all = memberRepository.findAll();
        List<MemberListSubResponseDto> dtos = new ArrayList<>();

        for (Member member : all) {
            MemberListSubResponseDto dto = new MemberListSubResponseDto();
            dto.setId(member.getId());
            dto.setLoginId(member.getLoginId());
            dto.setEmail(member.getEmail());
            dto.setProvider(member.getProvider());
            if (member.getNickname() == null) {
            } else {
                dto.setNickname(member.getNickname());
            }
            if (member.getResidence() == null) {
            } else {
                dto.setResidence(member.getResidence().name());
            }
            dto.setProfileImg(member.getProfileImg());
            dto.setRole(member.getRole());
            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/book/list/sub")
    public List<BookListSubResponseDto> bookListSub() {
        List<Book> all = bookRepository.findAll();
        ArrayList<BookListSubResponseDto> dtos = new ArrayList<>();

        for (Book book : all) {
            BookListSubResponseDto dto = new BookListSubResponseDto();
            dto.setId(book.getId());
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setPublisher(book.getPublisher());
            dto.setCoverImg(book.getCoverImg());
            dto.setIsbn(book.getIsbn());
            dto.setCreatedDate(book.getCreatedDate());
            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/review/list/sub")
    public List<ReviewListSubResponseDto> reviewListSub() {
        List<Review> all = reviewRepository.findAll();
        ArrayList<ReviewListSubResponseDto> dtos = new ArrayList<>();

        for (Review review : all) {
            ReviewListSubResponseDto dto = new ReviewListSubResponseDto();
            dto.setId(review.getId());
            dto.setTitle(review.getTitle());
            dto.setContent(review.getContent());
            dto.setScore(review.getScore());
            dto.setView(review.getView());
            dto.setCreatedDate(review.getCreatedDate());
            dto.setBookId(review.getBook().getId());
            dto.setMemberId(review.getMember().getId());

            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/sell/list/sub")
    public List<SellListSubResponseDto> sellListSub() {
        List<Sell> all = sellRepository.findAll();
        ArrayList<SellListSubResponseDto> dtos = new ArrayList<>();

        for (Sell sell : all) {
            SellListSubResponseDto dto = new SellListSubResponseDto();
            dto.setId(sell.getId());
            dto.setTitle(sell.getTitle());
            dto.setContent(sell.getContent());
            dto.setStatus(sell.getStatus());
            dto.setPrice(sell.getPrice());
            dto.setView(sell.getView());
            dto.setCreatedDate(sell.getCreatedDate());
            dto.setBookId(sell.getBook().getId());
            dto.setMemberId(sell.getMember().getId());

            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/home")
    public @ResponseBody
    String home() {
        return "<h1>home</h1>";
    }


    // user 권한만 접근 가능
    @GetMapping("/user")
    public String user(Authentication authentication) {
        String name = authentication.getName();
        return name;
    }
}
