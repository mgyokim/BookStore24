package bookstore24.v2.sell.service;

import bookstore24.v2.domain.Book;
import bookstore24.v2.domain.Member;
import bookstore24.v2.domain.Sell;
import bookstore24.v2.domain.SellStatus;
import bookstore24.v2.sell.repository.SellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    // 전체 판매 글을 페이징하여 반환
    public Page<Sell> getSellList(Pageable pageable) {
        return sellRepository.findAll(pageable);
    }

    @Transactional
    // Sell 삭제
    public void deleteSellById(Long sellId) {
        Optional<Sell> optionalSell = sellRepository.findById(sellId);
        if (optionalSell.isPresent()) {
            Sell sell = optionalSell.get();
            sell.logicalDelete();     // sell 엔티티 deleted 필드를 true 로 변경하여 논리적 삭제 진행
        }
    }

    // 특정 bookId 를 포함하는 Sell 찾기
    public List<Sell> findSellsByBookId(Long bookId) {
        return sellRepository.findAllByBook_Id(bookId);
    }

    // memberId 로 Sell 찾기
    public List<Sell> findAllByMemberId(Long memberId) {
        return sellRepository.findAllByMember_Id(memberId);
    }

    // member 와 SellStatus 로 Sell 찾기
    public Page<Sell> findSellsByMemberAndStatus(Member member, SellStatus status, Pageable pageable) {
        return sellRepository.findSellsByMemberAndStatus(member, status, pageable);
    }

    // Title 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByTitleKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Sell> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Sell> sells = sellRepository.findByTitleContaining(keyword, pageable);
            result.addAll(sells.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Sell> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

    // Book.title 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByBookTitleKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Sell> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Sell> sells = sellRepository.findByBook_TitleContaining(keyword, pageable);
            result.addAll(sells.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Sell> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }

    // Book.author 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByAuthorKeywords(String keywords, Pageable pageable) {
        // 검색어를 공백으로 분리하여 각각의 단어로 검색
        String[] keywordArray = keywords.split("\\s+");
        Set<Sell> result = new HashSet<>(); // 중복 제거용 Set
        for (String keyword : keywordArray) {
            Page<Sell> sells = sellRepository.findByBook_AuthorContaining(keyword, pageable);
            result.addAll(sells.getContent());
        }

        // 결과를 페이지네이션 적용
        List<Sell> resultList = new ArrayList<>(result);
        int fromIndex = Math.min(pageable.getPageNumber() * pageable.getPageSize(), resultList.size());
        int toIndex = Math.min((pageable.getPageNumber() + 1) * pageable.getPageSize(), resultList.size());
        return new PageImpl<>(resultList.subList(fromIndex, toIndex), pageable, resultList.size());
    }
}
