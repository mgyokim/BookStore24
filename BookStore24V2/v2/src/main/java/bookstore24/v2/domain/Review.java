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

    @OneToMany(mappedBy = "review")
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    protected Review() {
        //// JPA 에서 사용하기 위해 protected 생성자 유지
    }

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

    public Review(String title, String content, Long score) {
        this.title = title;
        this.content = content;
        this.score = score;
    }

    // 리뷰 글 작성후 리뷰 글의 상세를 최초 조회시 조회수를 1로 초기화
    public void initView() {
        // 값에 1을 더해서 설정
        this.view = 0L;
    }

    // 조회수를 1 증가시키기
    public void setView(Long view) {
        // 값에 1을 더해서 설정
        this.view = view + 1;
    }

    // 리뷰 글의 본문 수정
    public void editContent(String content) {
        this.content = content;
    }

    // 리뷰 글의 평점 수정
    public void editScore(Long score) {
        this.score = score;
    }

}
