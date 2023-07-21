package bookstore24.v2.oauth.logic;

import bookstore24.v2.auth.LocalSignUpDto;
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
        log.info("[로컬]회원가입 요청 데이터 로부터 Member 객체 생성 시작---------------------------------------------------");

        Member member = new Member();
        member.setLoginId(localSignUpDto.getLoginId());
        member.setLoginPassword(localSignUpDto.getLoginPassword());
        member.setEmail(localSignUpDto.getEmail());
        member.setProvider("bookstore24");
        member.setRole("ROLE_USER");

        log.info("[로컬]회원가입 요청 데이터 로부터 Member 객체 생성 완료---------------------------------------------------");

        return member;
    }

    /**
     * LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 성공
     */
    public Member joinCheck(Member localMember) {
        log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 시작---------------------------------------------------");

        Member duplicateLoginId = memberService.findMemberByLoginId(localMember.getLoginId());
        Member duplicateEmail = memberService.findMemberByEmail(localMember.getEmail());

        if ((duplicateLoginId == null) & (duplicateEmail == null)) {
            memberService.joinMember(localMember);
            log.info("로컬 회원가입이 완료되었습니다.");
            Member joinMember = memberService.findMemberByEmail(localMember.getEmail());
            log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 완료---------------------------------------------------");
            return joinMember;
        }
        if ((duplicateLoginId != null) & (duplicateEmail != null)) {
            log.info("LoginId 와 Email 이 모두 중복입니다.회원가입에 실패했습니다.");
            Member failCaseLoginIdEmail = new Member();
            log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 완료---------------------------------------------------");
            return failCaseLoginIdEmail;
        }
        if (duplicateLoginId != null) {
            log.info("LoginId 가 중복입니다. 회원가입에 실패했습니다.");
            Member failCaseLoginId = new Member();
            failCaseLoginId.setEmail(localMember.getEmail());
            log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 완료---------------------------------------------------");
            return failCaseLoginId;
        }
        if (duplicateEmail != null) {
            log.info("Email 이 중복입니다. 회원가입에 실패했습니다.");
            Member failCauseEmail = new Member();
            failCauseEmail.setLoginId(localMember.getLoginId());
            log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 완료---------------------------------------------------");
            return failCauseEmail;
        } else {
            log.info("회원가입에 실패했습니다. 이유는 LoginId, Email 중복 때문은 아님.");
            log.info("[로컬]LoginId, Email 각각 중복여부 체크하여 미중복시 회원가입 처리 완료---------------------------------------------------");
            return null;
        }
    }


}
