package bookstore24.v2.member.repository;


import bookstore24.v2.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // Jpa Query Method

    // select * from member where loginId = ?
    public Member findByLoginId(String loginId);

    // select * from member where email = ?
    public Member findByEmail(String email);

    // select * from member where nickname = ?
    public Member findByNickname(String nickname);

    // 논리적으로 삭제된 회원을 포함하지 않고 모든 회원을 조회
    List<Member> findAllByDeletedFalse();

    // 논리적으로 삭제된 회원만 조회
    List<Member> findAllByDeletedTrue();
}