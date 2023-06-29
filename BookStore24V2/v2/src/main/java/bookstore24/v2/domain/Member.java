package bookstore24.v2.domain;

import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SQLDelete(sql = "UPDATE Member SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String provider;    // 회원가입 유형[google, kakao, naver, local]

    private String providerId;  // OAuth 공급자 아이디

    private String loginId; // 로그인 아이디 (OAuth 자동 회원가입의 경우 {provider + "_" + providerId})

    private String loginPassword;   // 로그인 비밀번호

    private String email;   // 이메일

    private String role;    // 회원 등급

    private String nickName;    // 닉네임

    @Enumerated(EnumType.STRING)
    private Residence residence;   // 거주지 [seoul, incheon, gyeonggi]

    private String profileImg;  // 프로필 사진

    private boolean deleted = Boolean.FALSE;    // 삭제 여부

    @OneToMany(mappedBy = "member")
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Sell> sells = new ArrayList<>();

    @Builder
    public Member(String provider, String providerId, String loginId, String loginPassword, String email, String role) {
        this.provider = provider;
        this.providerId = providerId;
        this.loginId = loginId;
        this.loginPassword = loginPassword;
        this.email = email;
        this.role = role;
    }
}
