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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return sellRepository.findSellsByMemberAndStatusOrderByCreatedDateDesc(member, status, pageable);
    }

    // Title 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByTitleKeyword(String keyword, Pageable pageable) {
        // 해당 키워드로 데이터 총 개수를 조회
        long totalElements = sellRepository.countByTitleContaining(keyword);

        // 해당 키워드로 데이터를 조회하고 페이지네이션 적용
        Page<Sell> sells = sellRepository.findByTitleContaining(keyword, pageable);

        // Set 을 List 로 변환하고 정렬을 적용
        List<Sell> result = new ArrayList<>(sells.getContent());
        result.sort((sell1, sell2) -> sell2.getCreatedDate().compareTo(sell1.getCreatedDate()));

        return new PageImpl<>(result, pageable, totalElements);
    }

    // Book.title 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByBookTitleKeyword(String keyword, Pageable pageable) {
        // 해당 키워드로 데이터 총 개수를 조회
        long totalElements = sellRepository.countByBook_TitleContaining(keyword);

        // 해당 키워드로 데이터를 조회하고 페이지네이션 적용
        Page<Sell> sells = sellRepository.findByBook_TitleContaining(keyword, pageable);

        // Set 을 List 로 변환하고 정렬을 적용
        List<Sell> result = new ArrayList<>(sells.getContent());
        result.sort((sell1, sell2) -> sell2.getCreatedDate().compareTo(sell1.getCreatedDate()));

        return new PageImpl<>(result, pageable, totalElements);
    }

    // Book.author 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByAuthorKeyword(String keyword, Pageable pageable) {
        // 해당 키워드로 데이터 총 개수를 조회
        long totalElements = sellRepository.countByBook_AuthorContaining(keyword);

        // 해당 키워드로 데이터를 조회하고 페이지네이션 적용
        Page<Sell> sells = sellRepository.findByBook_AuthorContaining(keyword, pageable);

        // Set 을 List 로 변환하고 정렬을 적용
        List<Sell> result = new ArrayList<>(sells.getContent());
        result.sort((sell1, sell2) -> sell2.getCreatedDate().compareTo(sell1.getCreatedDate()));

        return new PageImpl<>(result, pageable, totalElements);
    }


    // Member.nickname 로 Sell 를 검색하고 페이지네이션 적용
    public Page<Sell> searchSellsByMemberNicknameKeyword(String keyword, Pageable pageable) {
        // 해당 키워드로 데이터 총 개수를 조회
        long totalElements = sellRepository.countByMember_NicknameContaining(keyword);

        // 해당 키워드로 데이터를 조회하고 페이지네이션 적용
        Page<Sell> sells = sellRepository.findByMember_NicknameContaining(keyword, pageable);

        // Set 을 List 로 변환하고 정렬을 적용
        List<Sell> result = new ArrayList<>(sells.getContent());
        result.sort((sell1, sell2) -> sell2.getCreatedDate().compareTo(sell1.getCreatedDate()));

        return new PageImpl<>(result, pageable, totalElements);
    }


    // SellStatus 가 On 인 Sells 찾기
    public Page<Sell> getAllSellsWithStatusOn(Pageable pageable) {
        // Call the custom repository method to retrieve "on" status sells with pagination
        return sellRepository.findByStatus(SellStatus.on, pageable);
    }

    // SellStatus 가 Off 인 Sells 찾기
    public Page<Sell> getAllSellsWithStatusOff(Pageable pageable) {
        // Call the custom repository method to retrieve "off" status sells with pagination
        return sellRepository.findByStatus(SellStatus.off, pageable);
    }
}
