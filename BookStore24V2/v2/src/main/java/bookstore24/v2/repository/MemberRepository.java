package bookstore24.v2.repository;


import bookstore24.v2.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    // Jpa Query Method

    // select * from member where loginId = ?
    public Member findByLoginId(String loginId);

}
