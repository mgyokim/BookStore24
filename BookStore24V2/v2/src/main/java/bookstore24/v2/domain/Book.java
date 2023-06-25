package bookstore24.v2.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

}
