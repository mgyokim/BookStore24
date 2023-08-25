package bookstore24.v2.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime modifiedDate;

    private boolean deleted = Boolean.FALSE;

    public void logicalDelete() {   // 논리적 삭제
        this.deleted = true;
    }

    public void logicalUnDelete() {  // 논리적 복구
        this.deleted = false;
    }

    public boolean isDeleted() {    // 회원 탈퇴 및 복구 상태 확인
        return deleted;
    }

}
