package bookstore24.v2.sell.service;

import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.sell.repository.SellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellService {

    private final SellRepository sellRepository;

    // sell title 중복 확인
    public Sell duplicateTitleSell(String title) {
        Sell duplicateTitleSell = sellRepository.findByTitle(title);
        return duplicateTitleSell;
    }

    @Transactional
    // sell 저장
    public Sell saveSell(Sell sell, Member member, Book book) {
        Sell savedSell = sellRepository.save(sell);
        sell.connectingSellAndBook(book);
        sell.connectingSellAndMember(member);
        return savedSell;
    }

    // 판매 글 작성자의 loginId 와 판매 글의 제목 title 을 이용하여 판매 글 찾기
    public Sell findByLoginIdAndTitle(String loginId, String title) {
        Sell sell = sellRepository.findByMemberLoginIdAndTitle(loginId, title);
        return sell;
    }
}
