package bookstore24.v2.domain;

import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@SQLDelete(sql = "UPDATE Sell SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Sell extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "sell_id")
    private Long id;

    private String title;

    private String content;

    private Long view;

    private Long price;

    @Enumerated(EnumType.STRING)
    private SellStatus status;  // on, off

    private String talkUrl;

    private boolean deleted = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    //==연관관계 편의 메서드 E==//
    public void connectingSellAndBook(Book book) {
        this.book = book;
        book.getSells().add(this);
    }

    //==연관관계 편의 메서드 F==//
    public void connectingSellAndMember(Member member) {
        this.member = member;
        member.getSells().add(this);
    }

    protected Sell() {
        //// JPA 에서 사용하기 위해 protected 생성자 유지
    }

    public Sell(String title, String content, Long price, SellStatus status, String talkUrl) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.talkUrl = talkUrl;
        this.status = status;
    }
}
