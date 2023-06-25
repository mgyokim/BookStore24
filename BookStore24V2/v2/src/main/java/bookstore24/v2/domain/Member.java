package bookstore24.v2.domain;

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

    @Enumerated(EnumType.STRING)
    private LoginType loginType;    // google, kakao, naver, local

    private String email;

    private String loginId;

    private String loginPassword;

    private String nickName;

    @Enumerated(EnumType.STRING)
    private Residence residence;   // seoul, incheon, gyeonggi

    private String profileImg;

    private boolean deleted = Boolean.FALSE;

    @OneToMany(mappedBy = "member")
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Sell> sells = new ArrayList<>();

}
