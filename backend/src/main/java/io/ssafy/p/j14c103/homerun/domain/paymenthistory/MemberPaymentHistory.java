package io.ssafy.p.j14c103.homerun.domain.paymenthistory;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_payment_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Access(AccessType.FIELD)
public class MemberPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false, length = 50)
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private MemberPaymentHistory(
            final Long userId,
            final String categoryId,
            final String categoryName,
            final String merchantName,
            final Integer paymentAmount,
            final LocalDate paymentDate
    ) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.merchantName = merchantName;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.createdAt = LocalDateTime.now();
    }

    public static MemberPaymentHistory create(
            final Long userId,
            final String categoryId,
            final String categoryName,
            final String merchantName,
            final Integer paymentAmount,
            final LocalDate paymentDate
    ) {
        return new MemberPaymentHistory(
                userId,
                categoryId,
                categoryName,
                merchantName,
                paymentAmount,
                paymentDate
        );
    }
}
