package io.ssafy.p.j14c103.homerun.domain.gamesession;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "game_sessions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_game_sessions__user_id__slot_number",
        columnNames = {"user_id", "slot_number"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_session_id")
    private Long gameSessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", nullable = false, length = 20)
    private CharacterType characterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type", nullable = false, length = 50)
    private HousingType housingType;

    @Column(name = "region_code", nullable = false, length = 30)
    private String regionCode;

    @Column(name = "district_code", nullable = false, length = 30)
    private String districtCode;

    @Column(name = "target_property_id", nullable = false)
    private Long targetPropertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source_type", nullable = false, length = 20)
    private DataSourceType dataSourceType;

    @Column(name = "current_turn", nullable = false)
    private Integer currentTurn;

    @Column(name = "\"current_date\"")
    private LocalDate currentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_phase", length = 20)
    private CyclePhase cyclePhase;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "cash_balance_amount", nullable = false)
    private Money cashBalance;

    @Convert(converter = Money.MoneyConverter.class)
    @Column(name = "net_worth_amount", nullable = false)
    private Money netWorth;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 20)
    private SessionStatus sessionStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_played_at")
    private LocalDateTime lastPlayedAt;

    private GameSession(
        final Long gameSessionId,
        final Long userId,
        final Integer slotNumber,
        final String characterName,
        final CharacterType characterType,
        final JobType jobType,
        final HousingType housingType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final DataSourceType dataSourceType,
        final Integer currentTurn,
        final LocalDate currentDate,
        final CyclePhase cyclePhase,
        final Money cashBalance,
        final Money netWorth,
        final SessionStatus sessionStatus,
        final LocalDateTime createdAt,
        final LocalDateTime lastPlayedAt
    ) {
        this.gameSessionId = gameSessionId;
        this.userId = userId;
        this.slotNumber = slotNumber;
        this.characterName = characterName;
        this.characterType = characterType;
        this.jobType = jobType;
        this.housingType = housingType;
        this.regionCode = regionCode;
        this.districtCode = districtCode;
        this.targetPropertyId = targetPropertyId;
        this.dataSourceType = dataSourceType;
        this.currentTurn = currentTurn;
        this.currentDate = currentDate;
        this.cyclePhase = cyclePhase;
        this.cashBalance = cashBalance;
        this.netWorth = netWorth;
        this.sessionStatus = sessionStatus;
        this.createdAt = createdAt;
        this.lastPlayedAt = lastPlayedAt;
    }

    public static GameSession create(
        final Long userId,
        final Integer slotNumber,
        final String characterName,
        final CharacterType characterType,
        final JobType jobType,
        final HousingType housingType,
        final String regionCode,
        final String districtCode,
        final Long targetPropertyId,
        final DataSourceType dataSourceType
    ) {
        return new GameSession(
            null,
            userId,
            slotNumber,
            characterName,
            characterType,
            jobType,
            housingType,
            regionCode,
            districtCode,
            targetPropertyId,
            dataSourceType,
            0,
            null,
            null,
            Money.zero(),
            Money.zero(),
            SessionStatus.IN_PROGRESS,
            LocalDateTime.now(),
            null
        );
    }

    public void initializeCapital(
        final Money cashBalance,
        final Money netWorth,
        final LocalDate startDate,
        final CyclePhase cyclePhase
    ) {
        this.cashBalance = cashBalance;
        this.netWorth = netWorth;
        this.currentDate = startDate;
        this.cyclePhase = cyclePhase;
    }

    public void assertOwner(final Long userId) {
        if (this.userId.equals(userId)) {
            return;
        }
        throw new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    public void assertInProgress() {
        if (sessionStatus == SessionStatus.IN_PROGRESS) {
            return;
        }
        throw new HomerunException(ErrorCode.GAME_SESSION_CLOSED);
    }

    public void advanceTurn(
        final Integer nextTurn,
        final LocalDate nextDate,
        final Money nextCash,
        final Money nextNetWorth,
        final CyclePhase nextPhase
    ) {
        this.currentTurn = nextTurn;
        this.currentDate = nextDate;
        this.cashBalance = nextCash;
        this.netWorth = nextNetWorth;
        this.cyclePhase = nextPhase;
    }

    public void markEnding(final SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
