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

    // 판매 글 작성후 리뷰 글의 상세를 최초 조회시 조회수를 1로 초기화
    public void initView() {
        // 값에 1을 더해서 설정
        this.view = 0L;
    }

    // 조회수를 1 증가시키기
    public void setView(Long view) {
        // 값에 1을 더해서 설정
        this.view = view + 1;
    }

    // 판매 글의 본문 수정
    public void editContent(String content) {
        this.content = content;
    }

    // 판매 글의 가격 수정
    public void editPrice(Long price) {
        this.price = price;
    }

    // 판매 글의 채팅 수정
    public void editTalkUrl(String talkUrl) {
        this.talkUrl = talkUrl;
    }

    // 판매 글의 상태를 on 으로 수정
    public void editSellStatusToOn() {
        this.status = SellStatus.on;
    }

    // 판매 글의 상태를 off 로 수정
    public void editSellStatusToOff() {
        this.status = SellStatus.off;
    }
}
