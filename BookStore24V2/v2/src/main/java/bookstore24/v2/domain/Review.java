package bookstore24.v2.domain;

import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SQLDelete(sql = "UPDATE Review SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "review_id")
    private Long id;

    private String title;

    private String content;

    private Long view;

    private Long score;

    private boolean deleted = Boolean.FALSE;

    @OneToMany(mappedBy = "review")
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    //==연관관계 편의 메서드 C==//
    public void connectingReviewAndMember(Member member) {
        this.member = member;
        member.getReviews().add(this);
    }

    //==연관관계 편의 메서드 D==//
    public void connectingReviewAndBook(Book book) {
        this.book = book;
        book.getReviews().add(this);
    }

}
