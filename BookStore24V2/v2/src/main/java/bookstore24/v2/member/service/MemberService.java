package bookstore24.v2.member.service;

import bookstore24.v2.book.service.BookService;
import bookstore24.v2.domain.*;
import bookstore24.v2.member.repository.MemberRepository;
import bookstore24.v2.review.service.ReviewService;
import bookstore24.v2.reviewcomment.service.ReviewCommentService;
import bookstore24.v2.sell.service.SellService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final CustomBCryptPasswordEncoder customBCryptPasswordEncoder;
    private final MemberRepository memberRepository;

    private final ReviewService reviewService;
    private final SellService sellService;
    private final BookService bookService;
    private final ReviewCommentService reviewCommentService;

    // 회원 가입
    @Transactional
    public void joinMember(Member member) {
        String rawPassword = member.getLoginPassword(); // 원문
        String encPassword = encodePassword(rawPassword);   // 해쉬
        member.registrationLoginPassword(encPassword);
        memberRepository.save(member);
    }

    // LoginId 으로 회원 조회
    public Member findMemberByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        return member;
    }

    // Email 으로 회원 조회
    public Member findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        return member;
    }

    // nickname 으로 회원 조회
    public Member findByNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);
        return member;
    }

    @Transactional
    // member 저장
    public Member saveMember(Member member) {
        Member savedMember = memberRepository.save(member);
        return savedMember;
    }

    // 입력받은 비밀번호 인코딩
    public String encodePassword(String password) {
        return customBCryptPasswordEncoder.encode(password);
    }

    // 비밀번호를 인코딩하여 저장된 비밀번호와 비교하는 메서드
    public boolean isPasswordMatch(String rawPassword, String dbPassword) {
        return customBCryptPasswordEncoder.matches(rawPassword, dbPassword);
    }

    @Transactional
    public void deleteMemberById(Long memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.logicalDelete();    // member 엔티티 deleted 필드를 true 로 변경하여 논리적 삭제진행
            memberRepository.save(member);
        }
    }

    @Transactional
    public void deleteMemberAndRelationInfoById(Long memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            // 해당 유저가 작성한 reviewComment 논리 삭제
            List<ReviewComment> allReviewCommentsByMemberId = reviewCommentService.findAllReviewCommentsByMemberId(memberId);
            if (allReviewCommentsByMemberId != null) {
                for (ReviewComment reviewComment : allReviewCommentsByMemberId) {
                    Long reviewCommentId = reviewComment.getId();
                    reviewCommentService.deleteReviewCommentById(reviewCommentId);
                }
            }

            // 해당 유저가 작성한 review 논리 삭제(해당 review 에 저장된 reviewComment 도 전부 함께)
            List<Review> allReviewByMemberId = reviewService.findAllReviewsByMemberId(memberId);
            if (allReviewByMemberId != null) {
                for (Review review : allReviewByMemberId) {
                    List<ReviewComment> reviewComments = review.getReviewComments();
                    if (reviewComments != null) {
                        for (ReviewComment reviewComment : reviewComments) {
                            Long reviewCommentId = reviewComment.getId();
                            reviewCommentService.deleteReviewCommentById(reviewCommentId);
                        }
                    }
                    Long reviewId = review.getId();
                    reviewService.deleteReviewById(reviewId);

                    // 해당 Review 에 등록한 책 데이터 삭제 여부 검사
                    Long bookId = review.getBook().getId();

                    // 해당 Review 를 삭제해도, 다른 Review 또는 Sell 에 등록된 책인 경우 아무것도 안함
                    // bookId 로 찾은 Sell
                    List<Sell> sellsByBookId = sellService.findSellsByBookId(bookId);
                    // bookId 로 찾은 Review
                    List<Review> reviewsByBookId = reviewService.findReviewsByBookId(bookId);

                    log.info(String.valueOf("sellsByBookId :" + sellsByBookId));
                    log.info(String.valueOf("reviewsByBookId : " + reviewsByBookId));

                    // 해당 Review 만 삭제하면 필요없어지는 책 데이터인 경우 -> 책 테이블에서 해당 책 데이터 논리 삭제
                    if ((sellsByBookId.size() == 0) & (reviewsByBookId.size() == 0)) {
                        log.info("[BookId : " + bookId + "] 데이터가 더이상 필요하지 않아서 논리 삭제");
                        bookService.deleteBookById(bookId);
                    }
                }
            }

            // 해당 유저가 작성한 sell 논리 삭제
            List<Sell> allSellsByMemberId = sellService.findAllByMemberId(memberId);
            if (allSellsByMemberId != null) {
                for (Sell sell : allSellsByMemberId) {
                    Long sellId = sell.getId();
                    sellService.deleteSellById(sellId);

                    // 해당 Sell 에 등록한 책 데이터 삭제 여부 검사
                    Long bookId = sell.getBook().getId();

                    // 해당 Sell 를 삭제해도, 다른 Sell 또는 Review 에 등록된 책인 경우 아무것도 안함
                    // bookId 로 찾은 Sell
                    List<Sell> sellsByBookId = sellService.findSellsByBookId(bookId);
                    // bookId 로 찾은 Review
                    List<Review> reviewsByBookId = reviewService.findReviewsByBookId(bookId);

                    log.info(String.valueOf("sellsByBookId :" + sellsByBookId));
                    log.info(String.valueOf("reviewsByBookId : " + reviewsByBookId));

                    // 해당 Sell 만 삭제하면 필요없어지는 책 데이터인 경우 -> 책 테이블에서 해당 책 데이터 논리 삭제
                    if ((sellsByBookId.size() == 0) & (reviewsByBookId.size() == 0)) {
                        log.info("[BookId : " + bookId + "] 데이터가 더이상 필요하지 않아서 논리 삭제");
                        bookService.deleteBookById(bookId);
                    }
                }
            }
        }
    }

    public Page<Sell> findSellsByMemberAndStatus(Member member, SellStatus status, Pageable pageable) {
        Page<Sell> sellsByMemberAndStatus = sellService.findSellsByMemberAndStatus(member, status, pageable);
        return sellsByMemberAndStatus;
    }

    public Page<Review> findReviewsByMember(Member member, Pageable pageable) {
        Page<Review> reviewsByMember = reviewService.findReviewsByMember(member, pageable);
        return reviewsByMember;
    }

}
