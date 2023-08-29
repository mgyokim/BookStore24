package bookstore24.v2.domain;

import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SQLDelete(sql = "UPDATE Book SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Book extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "book_id")
    private Long id;

    private Long isbn;

    private String title;

    private String author;

    private String publisher;

    private String coverImg;

    @OneToMany(mappedBy = "book")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book")
    private List<Sell> sells = new ArrayList<>();

    protected Book() {
        //// JPA 에서 사용하기 위해 protected 생성자 유지
    }

    // 도서 리뷰 등록할 때, 해당 도서가 데이터베이스에 존재하지 않을 때, ReviewPostSaveRequestDto 로 부터 데이터를 받아서 Book 생성
    public Book(Long isbn, String title, String author, String publisher, String coverImg) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.coverImg = coverImg;
    }

}
