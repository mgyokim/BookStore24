package bookstore.bookstoreprototype.service.query;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class OrderQueryService {

    // (OSIV를 끄면, 모든 지연로딩을 트랜잭션 안에서 돌려야한다. DTO도 같은 패키지로 가져와서..)
}
