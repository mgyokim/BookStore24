package bookstore24.v2.service;

import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
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
        String encPassword = customBCryptPasswordEncoder.encode(rawPassword);   // 해쉬
        member.registrationLoginPassword(encPassword);
        memberRepository.save(member);
    }

    // LoginId로 회원 조회
    public Member findMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        return member;
    }
}
