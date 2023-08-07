package bookstore24.v2.member.service;

import bookstore24.v2.domain.Member;
import bookstore24.v2.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final CustomBCryptPasswordEncoder customBCryptPasswordEncoder;
    private final MemberRepository memberRepository;

    // 회원 가입
    @Transactional
    public void joinMember(Member member) {
        String rawPassword = member.getLoginPassword(); // 원문
        String encPassword = encodePassword(rawPassword);   // 해쉬
        member.registrationLoginPassword(encPassword);
        memberRepository.save(member);
    }

    // LoginId 으로 회원 조회
    public Member findMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        return member;
    }

    // Email 으로 회원 조회
    public Member findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        return member;
    }

    // nickname 으로 회원 조회
    public Member findByNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);
        return member;
    }

    @Transactional
    // member 저장
    public Member saveMember(Member member) {
        Member savedMember = memberRepository.save(member);
        return savedMember;
    }

    // 입력받은 비밀번호 인코딩
    public String encodePassword(String password) {
        return customBCryptPasswordEncoder.encode(password);
    }

    // 비밀번호를 인코딩하여 저장된 비밀번호와 비교하는 메서드
    public boolean isPasswordMatch(String rawPassword, String dbPassword) {
        return customBCryptPasswordEncoder.matches(rawPassword, dbPassword);
    }


}
