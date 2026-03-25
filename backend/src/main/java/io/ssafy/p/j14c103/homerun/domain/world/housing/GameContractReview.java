package io.ssafy.p.j14c103.homerun.domain.world.housing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "game_contract_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameContractReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_contract_review_id")
    private Long id;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ContractReviewStatus reviewStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checked_traps")
    private List<String> checkedTraps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detected_traps")
    private List<String> detectedTraps;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_result_type")
    private ContractResult contractResult;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    private GameContractReview(
        Long gameSessionId,
        Long propertyId,
        ContractReviewStatus reviewStatus,
        List<String> checkedTraps,
        List<String> detectedTraps,
        ContractResult contractResult,
        LocalDateTime reviewedAt
    ) {
        this.gameSessionId = gameSessionId;
        this.propertyId = propertyId;
        this.reviewStatus = reviewStatus;
        this.checkedTraps = checkedTraps;
        this.detectedTraps = detectedTraps;
        this.contractResult = contractResult;
        this.reviewedAt = reviewedAt;
    }

    public static GameContractReview create(
        Long gameSessionId,
        Long propertyId,
        ContractReviewStatus reviewStatus,
        List<String> checkedTraps,
        List<String> detectedTraps,
        ContractResult contractResult,
        LocalDateTime reviewedAt
    ) {
        return new GameContractReview(
            gameSessionId,
            propertyId,
            reviewStatus,
            checkedTraps,
            detectedTraps,
            contractResult,
            reviewedAt
        );
    }
}
