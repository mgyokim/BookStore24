package bookstore24.v2.member.repository;


import bookstore24.v2.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // Jpa Query Method

    // select * from member where loginId = ?
    public Member findByLoginId(String loginId);

    // select * from member where email = ?
    public Member findByEmail(String email);

    // select * from member where nickname = ?
    public Member findByNickname(String nickname);
}
ㄷ