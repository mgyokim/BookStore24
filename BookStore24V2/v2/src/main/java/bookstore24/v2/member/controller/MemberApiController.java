package bookstore24.v2.member.controller;

import bookstore24.v2.auth.local.dto.LocalSignUpRequestDto;
import bookstore24.v2.auth.local.dto.LocalSignUpResponseDto;
import bookstore24.v2.auth.local.logic.LocalLogic;
import bookstore24.v2.auth.oauth.dto.token.GoogleOauthTokenDto;
import bookstore24.v2.auth.oauth.dto.token.KakaoOauthTokenDto;
import bookstore24.v2.auth.oauth.logic.GoogleLogic;
import bookstore24.v2.auth.oauth.logic.KakaoLogic;
import bookstore24.v2.auth.oauth.logic.NaverLogic;
import bookstore24.v2.auth.oauth.dto.token.NaverOauthTokenDto;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Residence;
import bookstore24.v2.member.dto.*;
import bookstore24.v2.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MemberApiController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;
    private final GoogleLogic googleLogic;
    private final LocalLogic localLogic;

    private final MemberService memberService;

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
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");
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
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

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
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

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

        log.info("[START] - MemberApiController.saveNickname / 닉네임 및 거주지 정보 저장 요청 시작");
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

        if (duplicateNiname == null) {
            member.setNickname(nickname);
        } else {
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
            log.info("[END] - MemberApiController.saveNickname / 닉네임 및 거주지 정보 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid Region");
        }

        Member saveMember = memberService.saveMember(member);

        SaveNicknameAndResidenceResponseDto saveNicknameAndResidenceResponseDto = new SaveNicknameAndResidenceResponseDto();
        saveNicknameAndResidenceResponseDto.setLoginId(JwtLoginId);
        saveNicknameAndResidenceResponseDto.setNickname(saveMember.getNickname());
        saveNicknameAndResidenceResponseDto.setResidence(String.valueOf(saveMember.getResidence()));

        log.info("닉네임 및 거주지 정보 저장 성공");
        log.info("[END] - MemberApiController.saveNickname / 닉네임 및 거주지 정보 저장 요청 종료");
        return ResponseEntity.status(HttpStatus.OK).body(saveNicknameAndResidenceResponseDto);
    }

    @GetMapping("/member/nicknameresidence/check")
    public ResponseEntity<String> checkNicknameAndResidence(Authentication authentication) {
        log.info("[START] - MemberApiController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 해당 회원의 정보를 조회하여 nickname 필드와 residence 에 값 null 인지, 값이 저장되어 있는지 상태를 반환
        String nickname = member.getNickname();
        Residence residence = member.getResidence();

        if ((nickname != null) & (residence != null)) {
            log.info("닉네임, 거주지 둘다 NOT NULL");
            log.info("[END] - MemberApiController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.OK).body("[NICKNAME : " + nickname + ", RESIDENCE : " + residence + "]");
        } else if ((nickname == null) & (residence == null)) {
            log.info("닉네임, 거주지 둘다 NULL");
            log.info("[END] - MemberApiController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : NULL, RESIDENCE : NULL]");
        } else if ((nickname != null) & (residence == null)) {
            log.info("거주지만 NULL");
            log.info("[END] - MemberApiController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : " + nickname + ", RESIDENCE : NULL]");
        } else {
            log.info("닉네임만 NULL");
            log.info("[END] - MemberApiController.checkNicknameAndResidence / 닉네임 및 거주지 정보 조회 요청 완료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("[NICKNAME : NULL, RESIDENCE : " + residence + "]");
        }
    }

    @GetMapping("/member/profile/edit")
    public ResponseEntity<AccessProfileEditResponseDto> accessProfileEdit(Authentication authentication) {
        log.info("[START] - MemberApiController.accessProfileEdit / 회원의 프로필 수정 접근(프로필 사진, 닉네임, 거주지역) 데이터 요청 시작");

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

        log.info("[END] - MemberApiController.accessProfileEdit / 회원의 프로필 수정 접근(프로필 사진, 닉네임, 거주지역) 데이터 요청 완료");

        return ResponseEntity.status(HttpStatus.OK).body(accessProfileEditResponseDto);
    }

    @Transactional
    @PostMapping("/member/profile/nickname/edit/save")
    public ResponseEntity<?> nicknameEditSave(Authentication authentication, @RequestBody @Valid NicknameEditSaveRequestDto nicknameEditSaveRequestDto) {
        log.info("[START] - MemberApiController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 시작");

        // JWT 를 이용하여 요청한 회원 확인
        String JwtLoginId = authentication.getName();
        Member member = memberService.findMemberByLoginId(JwtLoginId);

        // 수정을 요청한 닉네임
        String requestEditNickname = nicknameEditSaveRequestDto.getNickname();

        // 수정을 요청한 닉네임을 사용중인 회원 조회
        Member duplicateNicknameMember = memberService.findByNickname(requestEditNickname);

        // 수정을 요청한 닉네임을 사용중인 회원의 loginId
//        String duplicateNicknameMemberLoginId = duplicateNicknameMember.getLoginId();

        if (duplicateNicknameMember == null) {  // 닉네임 미중복 -> 수정 성공
            member.setNickname(requestEditNickname);
            NicknameEditSaveResponseDto nicknameEditSaveResponseDto = new NicknameEditSaveResponseDto();
            nicknameEditSaveResponseDto.setLoginId(JwtLoginId);
            nicknameEditSaveResponseDto.setNickname(requestEditNickname);
            log.info("닉네임 수정 성공");
            log.info("[END] - MemberApiController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.OK).body(nicknameEditSaveResponseDto);
        } else if ((duplicateNicknameMember != null) & (duplicateNicknameMember.getLoginId() == JwtLoginId)) {    // 회원의 기존 닉네임과 일치 -> 수정 실패
            log.info("회원의 기존 닉네임과 일치 -> 수정 실패");
            log.info("[END] - MemberApiController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용중인 기존 닉네임과 일치");
        } else if (duplicateNicknameMember != null) {   // 다른 회원과 닉네임 중복 -> 수정 실패
            log.info("[END] - MemberApiController.nicknameEditSave / 회원의 프로필 닉네임 수정 저장 요청 종료");
            log.info("다른 회원의 닉네임과 중복 -> 수정 실패");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 회원과 닉네임 중복");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("닉네임 수정 실패 원인을 개발자에게 문의");
    }
}
