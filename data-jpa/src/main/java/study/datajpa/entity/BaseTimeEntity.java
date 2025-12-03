package study.datajpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class) // JPA야 Auditing 기능 켜줭
public class BaseTimeEntity {

    @CreatedDate // 언제 처음 DB에 INSERT된건지
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate // 엔티티를 저장하거나 업데이트할 때 갱신
    private LocalDateTime lastModifiedDate;
}
