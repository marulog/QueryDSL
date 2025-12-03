package study.datajpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)  // JPA야 Auditing 기능 켜줭
public class BaseEntity  extends BaseTimeEntity{

    @CreatedBy // 누가 생성 했는지
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy // 누가 수정 했는지
    private String lastModifiedBy;
}
