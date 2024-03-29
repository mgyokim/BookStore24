package bookstore24.v2.domain;

import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@SQLDelete(sql = "UPDATE ReviewComment SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "ReviewComment_id")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //==연관관계 편의 메서드 A==//
    public void connectingReviewCommentAndReview(Review review) {
        this.review = review;
        review.getReviewComments().add(this);
    }

    //==연관관계 편의 메서드 B==//
    public void connectingReviewCommentAndMember(Member member) {
        this.member = member;
        member.getReviewComments().add(this);
    }

    protected ReviewComment() {
        //// JPA 에서 사용하기 위해 protected 생성자 유지
    }

    public ReviewComment(String content) {
        this.content = content;
    }

    // 리뷰 댓글의 본문 수정
    public void editContent(String content) {
        this.content = content;
    }

}
