package bookstore24.v2.auth.local.logic;

import bookstore24.v2.auth.local.LocalSignUpDto;
import bookstore24.v2.domain.Member;
import bookstore24.v2.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalLogic {

    private final MemberService memberService;

    /**
     * 회원 정보 생성
     */
    public Member requestJsonToMember(LocalSignUpDto localSignUpDto) {
        log.info("[START] - LocalLogic.requestJsonToMember / 클라이언트 요청 데이터 [loginId : " + localSignUpDto.getLoginId() + ", loginPassword : " + localSignUpDto.getLoginPassword() + ", email : " + localSignUpDto.getEmail() + "] 이용하여 Member 객체 생성 시작---------------------------------------------------------------------------------");

        Member member = new Member();
        member.setLoginId(localSignUpDto.getLoginId());
        member.setLoginPassword(localSignUpDto.getLoginPassword());
        member.setEmail(localSignUpDto.getEmail());
        member.setProvider("bookstore24");
        member.setRole("ROLE_USER");

        log.info("[END] - LocalLogic.requestJsonToMember / 클라이언트 요청 데이터 [loginId : " + localSignUpDto.getLoginId() + ", loginPassword : " + localSignUpDto.getLoginPassword() + ", email : " + localSignUpDto.getEmail() + "] 이용하여 Member 객체 생성 완료-----------------------------------------------------------------------------------");
        return member;
    }

    /**
     * LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 성공
     */
    public Member joinCheck(Member localMember) {
        log.info("[START] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 시작 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

        Member duplicateLoginId = memberService.findMemberByLoginId(localMember.getLoginId());
        Member duplicateEmail = memberService.findMemberByEmail(localMember.getEmail());

        if ((duplicateLoginId == null) & (duplicateEmail == null)) {
            log.info("[START] - LocalLogic.joinCheck (duplicateLoginId == null) & (duplicateEmail == null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 둘다 미중복임 -> 회원가입 진행---------------------------------------------------");

            memberService.joinMember(localMember);
            Member joinMember = memberService.findMemberByEmail(localMember.getEmail());

            log.info("[END] - LocalLogic.joinCheck (duplicateLoginId == null) & (duplicateEmail == null) / LoginId : [" + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 둘다 미중복임 -> 회원가입 완료---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

            return joinMember;
        }
        if ((duplicateLoginId != null) & (duplicateEmail != null)) {
            log.info("[START] - LocalLogic.joinCheck (duplicateLoginId != null) & (duplicateEmail != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 둘다 중복임 -> 회원가입 실패 객체 생성 시작---------------------------------------------------");

            Member failCaseLoginIdEmail = new Member();

            log.info("[END] - LocalLogic.joinCheck (duplicateLoginId != null) & (duplicateEmail != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 둘다 중복임 -> 회원가입 실패 객체 생성 완료---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return failCaseLoginIdEmail;
        }
        if (duplicateLoginId != null) {
            log.info("[START] - LocalLogic.joinCheck (duplicateLoginId != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] loginId 가 중복임 -> 회원가입 실패 객체 생성 시작---------------------------------------------------");

            Member failCaseLoginId = new Member();
            failCaseLoginId.setEmail(localMember.getEmail());

            log.info("[END] - LocalLogic.joinCheck (duplicateLoginId != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] loginId 가 중복임 -> 회원가입 실패 객체 생성 완료---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

            return failCaseLoginId;
        }
        if (duplicateEmail != null) {
            log.info("[START] - LocalLogic.joinCheck (duplicateEmail != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] email 가 중복임 -> 회원가입 실패 객체 생성 시작---------------------------------------------------");
            Member failCauseEmail = new Member();
            failCauseEmail.setLoginId(localMember.getLoginId());

            log.info("[END] - LocalLogic.joinCheck (duplicateEmail != null) / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] email 가 중복임 -> 회원가입 실패 객체 생성 완료---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return failCauseEmail;
        } else {
            log.info("[START] - LocalLogic.joinCheck 특수케이스임 체크 필수!!! / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 특수케이스임!!! -> 회원가입 실패 특수케이스임!!---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck 특수케이스임 체크 필수!!! / [LoginId : " + localMember.getLoginId() + ", Email : " + localMember.getEmail() + "] 특수케이스임!!! -> 회원가입 실패 특수케이스임!!---------------------------------------------------");
            log.info("[END] - LocalLogic.joinCheck / [LoginId : " + localMember.getLoginId() + ", email : " + localMember.getEmail() + "]  각각 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return null;
        }
    }


}
