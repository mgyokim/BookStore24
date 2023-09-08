package bookstore24.v2.member.controller;

import bookstore24.v2.auth.local.dto.LocalSignUpRequestDto;
import bookstore24.v2.auth.local.dto.LocalSignUpResponseDto;
import bookstore24.v2.auth.local.logic.LocalLogic;
import bookstore24.v2.auth.oauth.dto.token.GoogleOauthTokenDto;
import bookstore24.v2.auth.oauth.dto.token.KakaoOauthTokenDto;
import bookstore24.v2.auth.oauth.dto.token.NaverOauthTokenDto;
import bookstore24.v2.auth.oauth.logic.GoogleLogic;
import bookstore24.v2.auth.oauth.logic.KakaoLogic;
import bookstore24.v2.auth.oauth.logic.NaverLogic;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Residence;
import bookstore24.v2.domain.SellStatus;
import bookstore24.v2.member.dto.*;
import bookstore24.v2.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;
    private final GoogleLogic googleLogic;
    private final LocalLogic localLogic;

    private final MemberService memberService;

    @GetMapping("/")
    public ResponseEntity<?> main() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping("/auth/kakao/callback")
    public ResponseEntity<String> kakaoLogin(@RequestParam(value = "Authorization_code", required = true) String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthTokenDto kakaoOauthTokenDto = kakaoLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogic.accessTokenToProfile(kakaoOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = kakaoLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = kakaoLogic.kakaoAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("kakaoLogin 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = kakaoLogic.kakaoAutoLoginFail(email, provider);

            log.info("kakaoLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @PostMapping("/auth/naver/callback")
    public ResponseEntity<String> naverLogin(@RequestParam(value = "Authorization_code", required = true) String code) {

        // 발급받은 인가 코드로 토큰 요청
        NaverOauthTokenDto naverOauthTokenDto = naverLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = naverLogic.accessTokenToProfile(naverOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = naverLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = naverLogic.naverAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("naverLogin 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = naverLogic.naverAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @PostMapping("/auth/google/callback")
    public ResponseEntity<String> googleLogin(@RequestParam(value = "Authorization_code", required = true) String code) {

        // 발급받은 인가 코드로 토큰 요청
        GoogleOauthTokenDto googleOauthTokenDto = googleLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = googleLogic.accessTokenToProfile(googleOauthTokenDto);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = googleLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = googleLogic.googleAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("googleLogin 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = googleLogic.googleAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);

            return failResponseJwt;
        }
    }

    @PostMapping("/local/signup")
    public ResponseEntity<?> localSignUp(@RequestBody @Valid LocalSignUpRequestDto localSignUpRequestDto,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류가 있는 경우 에러 응답 변환
            return ResponseEntity.badRequest().body("Invalid signup request");
        }

        // 회원 정보 생성
        Member member = localLogic.requestJsonToMember(localSignUpRequestDto);

        // 회원 가입 시도 (loginId, email 이 중복되지 않는 경우에만 회원가입 성공)
        Member tryJoinMember = localLogic.joinCheck(member);

        if ((tryJoinMember.getLoginId() != null) & (tryJoinMember.getEmail() != null)) {
            LocalSignUpResponseDto localSignUpResponseDto = new LocalSignUpResponseDto();
            localSignUpResponseDto.setMessage("회원가입 성공");
            localSignUpResponseDto.setLoginId(member.getLoginId());
            localSignUpResponseDto.setEmail(member.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(localSignUpResponseDto);
        }
        if ((tryJoinMember.getLoginId() == null) & (tryJoinMember.getEmail() != null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("로그인 아이디 중복 : 회원가입 실패");
        }
        if ((tryJoinMember.getLoginId() != null) & (tryJoinMember.getEmail() == null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일 중복 : 회원가입 실패");
        }
        if ((tryJoinMember.getLoginId() == null) & (tryJoinMember.getEmail() == null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("로그인 아이디, 이메일 중복 : 회원가입 실패");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("개발자 문의 요망 : 회원가입 실패");
        }
    }

    @Transactional
    @PostMapping("/member/nicknameresidence/save")
    public ResponseEntity<?> saveNicknameAndResidence(Authentication authentication, @RequestBody @Valid SaveNicknameAndResidenceRequestDto saveNicknameAndResidenceRequestDto) {

        log.info("[START] - MemberController.saveNickname / 닉네임 및 거주지 정보 저장 요청 시작");
        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        log.info("닉네임 및 거주지 정보 저장을 요청한 회원의 loginId : " + JwtLoginId);

        // 요청으로 받은 saveNicknameAndResidenceRequestDto 으로부터 nickname 과 residence 받기
        String nickname = saveNicknameAndResidenceRequestDto.getNickname();
        String residence = saveNicknameAndResidenceRequestDto.getResidence();
        log.info("loginId : " + JwtLoginId + " 가 저장을 요청한 nickname : " + nickname + ", residence : " + residence);


        // 등록을 요청한 닉네임 중복 여부 검사
        Member duplicateNiname = memberService.findByNickname(nickname);

        if (duplicateNiname == null) {  // 닉네임 중복 검사 통과시 다음 로직 진행
        } else {    // 닉네임 중복 검사 불통과시 리턴
            log.info("닉네임 및 거주지 정보 저장 실패 [Cause : 닉네임 중복]");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("duplicate Nickname");
        }

        // 요청 데이터를 처리하는 로직
        if (Residence.incheon.name().equals(residence)) {
            member.setResidence(Residence.incheon);
        } else if (Residence.seoul.name().equals(residence)) {
            member.setResidence(Residence.seoul);
        } else if (Residence.gyeonggi.name().equals(residence)) {
            member.setResidence(Residence.gyeonggi);
        } else {
            log.info("닉네임 및 거주지 정보 저장 실패 [Cause : 서비스 사용 불가 거주지]");
            log.info("[END] - MemberController.saveNickname / 닉네임 및 거주지 정보 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid Region");
        }

        member.setNickname(nickname);
        Member saveMember = memberService.saveMember(member);

        SaveNicknameAndResidenceResponseDto saveNicknameAndResidenceResponseDto = new SaveNicknameAndResidenceResponseDto();
        saveNicknameAndResidenceResponseDto.setLoginId(JwtLoginId);
        saveNicknameAndResidenceResponseDto.setNickname(saveMember.getNickname());
        saveNicknameAndResidenceResponseDto.setResidence(String.valueOf(saveMember.getResidence()));

        log.info("닉네임 및 거주지 정보 저장 성공");
        log.info("[END] - MemberController.saveNickname / 닉네임 및 거주지 정보 저장 요청 종료");
        return ResponseEntity.status(HttpStatus.OK).body(saveNicknameAndResidenceResponseDto);
    }

    @GetMapping("/member/nicknameresidence/check")
    public ResponseEntity<String> checkNicknameAndResidence(Authentication authentication) {
        log.info("[START] - MemberController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 해당 회원의 정보를 조회하여 nickname 필드와 residence 에 값 null 인지, 값이 저장되어 있는지 상태를 반환
        String nickname = member.getNickname();
        Residence residence = member.getResidence();

        if ((nickname != null) & (residence != null)) {
            log.info("닉네임, 거주지 둘다 NOT NULL");
            log.info("[END] - MemberController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.OK).body("[NICKNAME : " + nickname + ", RESIDENCE : " + residence + "]");
        } else if ((nickname == null) & (residence == null)) {
            log.info("닉네임, 거주지 둘다 NULL");
            log.info("[END] - MemberController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : NULL, RESIDENCE : NULL]");
        } else if ((nickname != null) & (residence == null)) {
            log.info("거주지만 NULL");
            log.info("[END] - MemberController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : " + nickname + ", RESIDENCE : NULL]");
        } else {
            log.info("닉네임만 NULL");
            log.info("[END] - MemberController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : NULL, RESIDENCE : " + residence + "]");
        }
    }

    @GetMapping("/member/profile/edit")
    public ResponseEntity<AccessProfileEditResponseDto> accessProfileEdit(Authentication authentication) {
        log.info("[START] - MemberController.accessProfileEdit / 회원의 프로필 수정 접근(프로필 사진, 닉네임, 거주지역) 데이터 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // DB에 저장되어있는 해당 회원의 정보
        String loginId = member.getLoginId();
        String email = member.getEmail();
        String nickname = member.getNickname();
        Residence residence = member.getResidence();
        String profileImg = member.getProfileImg();

        // 해당 회원의 정보를 DTO 에 담아서 Response
        AccessProfileEditResponseDto accessProfileEditResponseDto = new AccessProfileEditResponseDto();

        accessProfileEditResponseDto.setLoginId(loginId);
        accessProfileEditResponseDto.setEmail(email);
        accessProfileEditResponseDto.setNickname(nickname);
        accessProfileEditResponseDto.setResidence(String.valueOf(residence));
        accessProfileEditResponseDto.setProfileImg(profileImg);

        log.info("[END] - MemberController.accessProfileEdit / 회원의 프로필 수정 접근(프로필 사진, 닉네임, 거주지역) 데이터 요청 완료");

        return ResponseEntity.status(HttpStatus.OK).body(accessProfileEditResponseDto);
    }

    @Transactional
    @PostMapping("/member/profile/nickname/edit/save")
    public ResponseEntity<?> nicknameEditSave(Authentication authentication, @RequestBody @Valid NicknameEditSaveRequestDto nicknameEditSaveRequestDto) {
        log.info("[START] - MemberController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 수정을 요청한 닉네임
        String requestEditNickname = nicknameEditSaveRequestDto.getNickname();

        // 수정을 요청한 닉네임을 사용중인 회원 조회
        Member duplicateNicknameMember = memberService.findByNickname(requestEditNickname);

        if (duplicateNicknameMember == null) {  // 닉네임 미중복 -> 수정 성공
            member.setNickname(requestEditNickname);
            NicknameEditSaveResponseDto nicknameEditSaveResponseDto = new NicknameEditSaveResponseDto();
            nicknameEditSaveResponseDto.setLoginId(JwtLoginId);
            nicknameEditSaveResponseDto.setNickname(requestEditNickname);
            log.info("닉네임 수정 성공");
            log.info("[END] - MemberController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(nicknameEditSaveResponseDto);
        } else if ((duplicateNicknameMember != null) & (duplicateNicknameMember.getLoginId() == JwtLoginId)) {    // 회원의 기존 닉네임과 일치 -> 수정 실패
            log.info("회원의 기존 닉네임과 일치 -> 수정 실패");
            log.info("[END] - MemberController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용중인 기존 닉네임과 일치");
        } else if (duplicateNicknameMember != null) {   // 다른 회원과 닉네임 중복 -> 수정 실패
            log.info("[END] - MemberController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            log.info("다른 회원의 닉네임과 중복 -> 수정 실패");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 회원과 닉네임 중복");
        }
        log.info("[END] - MemberController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("닉네임 수정 실패 원인을 개발자에게 문의");
    }

    @Transactional
    @PostMapping("/member/profile/residence/edit/save")
    public ResponseEntity<?> residenceEditSave(Authentication authentication, @RequestBody @Valid ResidenceEditSaveRequestDto residenceEditSaveRequestDto) {

        log.info("[START] - MemberController.residenceEditSave / 회원의 프로필 거주지 수정 저장 요청 시작");
        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 회원의 기존 거주지
        Residence residence = member.getResidence();

        // 수정을 요청한 거주지
        String requestEditResidence = residenceEditSaveRequestDto.getResidence();

        // 요청 데이터를 처리하는 로직
        if (residence.name().equals(requestEditResidence)) {    // 수정을 요청한 거주지가 기존 거주지와 같으면, 수정 실패
            log.info("거주지 정보 수정 실패 [Cause : 기존 거주지와 일치]");
            log.info("[END] - MemberController.residenceEditSave / 회원의 프로필 거주지 수정 저장 요청 종료");

            return ResponseEntity.status(HttpStatus.CONFLICT).body("기존 거주지와 같은 거주지입니다. 수정 실패");
        } else {
            if ((Residence.incheon.name().equals(requestEditResidence)) || (Residence.seoul.name().equals(requestEditResidence)) || (Residence.gyeonggi.name().equals(requestEditResidence))) {    // 성공적으로 수정
                member.setResidence(Residence.valueOf(requestEditResidence));
                ResidenceEditSaveResponseDto residenceEditSaveResponseDto = new ResidenceEditSaveResponseDto();
                residenceEditSaveResponseDto.setLoginId(JwtLoginId);
                residenceEditSaveResponseDto.setResidence(requestEditResidence);

                log.info("거주지 정보 수정 성공 [LoginId : " + JwtLoginId + ", residence : " + requestEditResidence + "]");
                log.info("[END] - MemberController.residenceEditSave / 회원의 프로필 거주지 수정 저장 요청 종료");

                return ResponseEntity.status(HttpStatus.OK).body(residenceEditSaveResponseDto);
            } else {

                log.info("거주지 정보 수정 실패 [Cause : 서비스 사용 불가 거주지]");
                log.info("[END] - MemberController.residenceEditSave / 회원의 프로필 거주지 수정 저장 요청 종료");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid Region");
            }
        }
    }

    @Transactional
    @PostMapping("member/password/edit/save")
    public ResponseEntity<?> passwordEditSave(Authentication authentication, @RequestBody @Valid PasswordEditSaveRequestDto passwordEditSaveRequestDto) {
        log.info("[START] - MemberController.passwordEditSave / 회원의 비밀번호 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // DB 에 저장되어 있는 JwtLoginId 의 loginPassword
        String dbPassword = member.getLoginPassword();
        log.info("DB 에 인코딩 되어 저장되어 있는 [LoginId : " + JwtLoginId + " 의 LoginPassword : " + dbPassword + "]");

        // 비밀번호 수정을 요청한 회원이 본인검증을 위해 입력한 현재 비밀번호
        String nowPassword = passwordEditSaveRequestDto.getNowPassword();
        log.info("비밀번호 수정을 요청한 회원(" + JwtLoginId + ") 이 입력한 현재 loginPassword : " + nowPassword);

        //DB 에 저장되어 있는 loginPassword 와 사용자가 입력한 비밀번호 일치여부
        boolean nowPasswordAndDbPasswordMatch = memberService.isPasswordMatch(nowPassword, dbPassword);
        log.info("DB 에 저장되어 있는 loginPassword 와 회원이 검증을 위해 입력한 nowPassword 를 인코딩해서 비교한 일치여부 : " + nowPasswordAndDbPasswordMatch);

        if (nowPasswordAndDbPasswordMatch == false) {   // 현재 비밀번호 검증이 실패하면 응답으로 401 반환
            log.info("loginId : " + JwtLoginId + " 의 비밀번호 수정 실패 [Cause : 입력한 현재 비밀번호가 틀림]");
            log.info("[END] - MemberController.passwordEditSave / 회원의 비밀번호 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("입력한 현재 loginPassword 가 틀림");
        } else {    // 현재 비밀번호를 잘 입력한 요청이라면,
            String password1 = passwordEditSaveRequestDto.getPassword1();
            String password2 = passwordEditSaveRequestDto.getPassword2();
            String encodedPassword1 = memberService.encodePassword(password1);
            boolean nowPasswordAndPassword1Match = memberService.isPasswordMatch(nowPassword, encodedPassword1);

            if (nowPasswordAndPassword1Match == true) {     // 수정을 요청한 비밀번호가 기존 비밀번호와 일치하면 수정 실패
                log.info("loginId : " + JwtLoginId + " 의 비밀번호 수정 실패 [Cause : 기존 비밀번호와 동일한 비밀번호로 수정 요청함]");
                log.info("[END] - MemberController.passwordEditSave / 회원의 비밀번호 수정 저장 요청 종료");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("기존 비밀번호와 동일한 비밀번호로 수정을 요청함");
            } else {    // 수정을 요청한 비밀번호가 기존 비밀번호와 일치하지 않으면
                if (!(password1.equals(password2))) {   // 비밀번호1 과 비밀번호2 가 일치하지 않으면 수정 실패

                    log.info("loginId : " + JwtLoginId + " 의 비밀번호 수정 실패 [Cause : 비밀번호1, 비밀번호2 불일치]");
                    log.info("[END] - MemberController.passwordEditSave / 회원의 비밀번호 수정 저장 요청 종료");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("password1 와 password2 불일치");
                } else {    // 비밀번호1 과 비밀번호2 가 일치하면 수정 성공
                    String encodedPassword = memberService.encodePassword(password1);
                    member.setLoginPassword(encodedPassword);
                    PasswordEditSaveResponseDto passwordEditSaveResponseDto = new PasswordEditSaveResponseDto();
                    passwordEditSaveResponseDto.setLoginId(JwtLoginId);
                    log.info("loginId : " + JwtLoginId + " 의 비밀번호 수정 성공");
                    log.info("[END] - MemberController.passwordEditSave / 회원의 비밀번호 수정 저장 요청 종료");
                    return ResponseEntity.status(HttpStatus.OK).body(passwordEditSaveResponseDto);
                }
            }
        }
    }

    @Transactional
    @PostMapping("/member/withdraw")
    public ResponseEntity<?> memberWithdraw(Authentication authentication) {
        log.info("[START] - MemberController.memberWithdraw / 회원 탈퇴 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);
        Long memberId = member.getId();

        // 해당 회원 탈퇴 처리 전에 관련 데이터 논리 삭제
        memberService.deleteMemberAndRelationInfoById(memberId);

        // 해당 회원 탈퇴 처리
        memberService.deleteMemberById(memberId);

        // 탈퇴 처리된 회원 정보 DTO 에 넣어서 응답
        MemberWithdrawResponseDto memberWithdrawResponseDto = new MemberWithdrawResponseDto();
        memberWithdrawResponseDto.setLoginId(JwtLoginId);
        memberWithdrawResponseDto.setEmail(member.getEmail());

        log.info("[loginId : " + JwtLoginId + "] 탈퇴처리 완료");
        log.info("[END] - MemberController.memberWithdraw / 회원 탈퇴 요청 종료");
        return ResponseEntity.status(HttpStatus.OK).body(memberWithdrawResponseDto);
    }

    @GetMapping("/member/pofile/sell/on/list")
    public Page<MemberProfileSellOnListResponseDto> memberProfileSellOnList(Authentication authentication, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - MemberController.memberProfileSellOnList / 회원 프로필의 판매중인 도서 판매글 목록 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);
        Long memberId = member.getId();
        SellStatus status = SellStatus.on;

        Pageable pageable = PageRequest.of(page, size);
        log.info("[END] - MemberController.memberProfileSellOnList / 회원 프로필의 판매중인 도서 판매글 목록 요청 종료");
        return memberService.findSellsByMemberAndStatus(member, status, pageable)
                .map(sell -> {
                    MemberProfileSellOnListResponseDto memberProfileSellOnListResponseDto = new MemberProfileSellOnListResponseDto();
                    memberProfileSellOnListResponseDto.setId(sell.getId());
                    memberProfileSellOnListResponseDto.setTitle(sell.getTitle());
                    memberProfileSellOnListResponseDto.setStatus(sell.getStatus());
                    memberProfileSellOnListResponseDto.setCoverImg(sell.getBook().getCoverImg());
                    memberProfileSellOnListResponseDto.setBookTitle(sell.getBook().getTitle());
                    memberProfileSellOnListResponseDto.setAuthor(sell.getBook().getAuthor());
                    memberProfileSellOnListResponseDto.setPublisher(sell.getBook().getPublisher());
                    memberProfileSellOnListResponseDto.setPrice(sell.getPrice());
                    memberProfileSellOnListResponseDto.setNickname(sell.getMember().getNickname());
                    memberProfileSellOnListResponseDto.setLoginId(sell.getMember().getLoginId());
                    memberProfileSellOnListResponseDto.setCreatedDate(sell.getCreatedDate());
                    memberProfileSellOnListResponseDto.setView(sell.getView());
                    return memberProfileSellOnListResponseDto;
                });
    }

    @GetMapping("/member/pofile/sell/off/list")
    public Page<MemberProfileSellOffListResponseDto> memberProfileSellOffList(Authentication authentication, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - MemberController.memberProfileSellOffList / 회원 프로필의 판매완료 도서 판매글 목록 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);
        Long memberId = member.getId();
        SellStatus status = SellStatus.off;

        Pageable pageable = PageRequest.of(page, size);
        log.info("[END] - MemberController.memberProfileSellOffList / 회원 프로필의 판매완료 도서 판매글 목록 요청 종료");
        return memberService.findSellsByMemberAndStatus(member, status, pageable)
                .map(sell -> {
                    MemberProfileSellOffListResponseDto memberProfileSellOffListResponseDto = new MemberProfileSellOffListResponseDto();
                    memberProfileSellOffListResponseDto.setId(sell.getId());
                    memberProfileSellOffListResponseDto.setTitle(sell.getTitle());
                    memberProfileSellOffListResponseDto.setStatus(sell.getStatus());
                    memberProfileSellOffListResponseDto.setCoverImg(sell.getBook().getCoverImg());
                    memberProfileSellOffListResponseDto.setBookTitle(sell.getBook().getTitle());
                    memberProfileSellOffListResponseDto.setAuthor(sell.getBook().getAuthor());
                    memberProfileSellOffListResponseDto.setPublisher(sell.getBook().getPublisher());
                    memberProfileSellOffListResponseDto.setPrice(sell.getPrice());
                    memberProfileSellOffListResponseDto.setNickname(sell.getMember().getNickname());
                    memberProfileSellOffListResponseDto.setLoginId(sell.getMember().getLoginId());
                    memberProfileSellOffListResponseDto.setCreatedDate(sell.getCreatedDate());
                    memberProfileSellOffListResponseDto.setView(sell.getView());
                    return memberProfileSellOffListResponseDto;
                });
    }

    @GetMapping("member/profile/review/list")
    public Page<MemberProfileReviewListResponseDto> memberProfileReviewList(Authentication authentication, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("[START] - MemberController.memberProfileReviewList / 회원 프로필의 작성한 도서 후기 목록 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);
        Long memberId = member.getId();

        Pageable pageable = PageRequest.of(page, size);

        log.info("[END] - MemberController.memberProfileReviewList / 회원 프로필의 작성한 도서 후기 목록 요청 종료");
        return memberService.findReviewsByMember(member, pageable)
                .map(review -> {
                    MemberProfileReviewListResponseDto memberProfileReviewListResponseDto = new MemberProfileReviewListResponseDto();
                    memberProfileReviewListResponseDto.setId(review.getId());
                    memberProfileReviewListResponseDto.setTitle(review.getTitle());
                    memberProfileReviewListResponseDto.setScore(review.getScore());
                    memberProfileReviewListResponseDto.setCoverImg(review.getBook().getCoverImg());
                    memberProfileReviewListResponseDto.setBookTitle(review.getBook().getTitle());
                    memberProfileReviewListResponseDto.setAuthor(review.getBook().getAuthor());
                    memberProfileReviewListResponseDto.setPublisher(review.getBook().getPublisher());
                    memberProfileReviewListResponseDto.setNickname(review.getMember().getNickname());
                    memberProfileReviewListResponseDto.setLoginId(review.getMember().getLoginId());
                    memberProfileReviewListResponseDto.setCreatedDate(review.getCreatedDate());
                    memberProfileReviewListResponseDto.setView(review.getView());
                    return memberProfileReviewListResponseDto;
                });
    }

    @GetMapping("/member/profile/my")
    public ResponseEntity<?> memberProfileMy(Authentication authentication) {
        log.info("[START] - MemberController.memberProfileMy / 회원 프로필 마이페이지 요청");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        Long memberId = member.getId();
        String email = member.getEmail();
        String provider = member.getProvider();
        String nickname = member.getNickname();
        String residence = String.valueOf(member.getResidence());
        String profileImg = member.getProfileImg();

        MemberProfileMyResponseDto memberProfileMyResponseDto = new MemberProfileMyResponseDto();
        memberProfileMyResponseDto.setId(memberId);
        memberProfileMyResponseDto.setEmail(email);
        memberProfileMyResponseDto.setProvider(provider);
        memberProfileMyResponseDto.setNickname(nickname);
        memberProfileMyResponseDto.setResidence(residence);
        memberProfileMyResponseDto.setProfileImg(profileImg);

        log.info("[END] - MemberController.memberProfileMy / 회원 프로필 마이페이지 종료");

        return ResponseEntity.status(HttpStatus.OK).body(memberProfileMyResponseDto);
    }
}
