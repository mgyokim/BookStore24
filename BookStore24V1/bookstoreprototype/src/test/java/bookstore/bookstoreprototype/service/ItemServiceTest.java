package bookstore.bookstoreprototype.service;

import bookstore.bookstoreprototype.domain.item.Book;
import bookstore.bookstoreprototype.domain.item.Item;
import bookstore.bookstoreprototype.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    EntityManager em;

    @Test
    public void 상품등록() throws Exception {
        //given
        Book book = createBookObj("김민교 JPA", 10000, 10);

        //when
        Long savedId = itemService.saveItem(book);

        //then
        em.flush(); // 쿼리 확인용
        assertEquals(book, itemRepository.findOne(savedId));
    }

    @Test
    public void 상품수정() throws Exception {
        //given
        Book book = createBookObj("김민교 JPA", 10000, 10);
        Long savedId = itemService.saveItem(book);

        //when
        book.setName("김민교 JPA 수정");
        book.setPrice(20000);
        book.setStockQuantity(20);
        Long savedId2 = itemService.saveItem(book);

        //then
        em.flush(); // 쿼리 확인용
        assertEquals(itemRepository.findOne(savedId), itemRepository.findOne(savedId2));
        assertEquals(itemRepository.findOne(savedId).getName(), "김민교 JPA 수정");
        assertEquals(itemRepository.findOne(savedId).getPrice(), 20000);
        assertEquals(itemRepository.findOne(savedId).getStockQuantity(), 20);
    }

    @Test
    public void 전체상품조회() throws Exception {
        //given
        Book book1 = createBookObj("김민교 JPA", 10000, 10);
        itemService.saveItem(book1);
        Book book2 = createBookObj("김민교 JPA2", 20000, 20);
        itemService.saveItem(book2);

        //when
        List<Item> items = itemService.findItems();

        //then
    }

    @Test
    public void 상품단건조회() throws Exception {
        //given
        Book book = createBookObj("김민교 JPA", 10000, 10);
        Long savedId = itemService.saveItem(book);

        //when
        Item findItem = itemService.findOne(savedId);

        //then
        assertEquals(findItem, book);
    }


    private Book createBookObj(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        return book;
    }
}
