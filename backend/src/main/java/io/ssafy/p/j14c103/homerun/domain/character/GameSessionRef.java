package io.ssafy.p.j14c103.homerun.domain.character;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.housing.HousingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "유저생성게임")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameSessionRef {

    @Id
    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Column(name = "유저번호", nullable = false)
    private Integer userId;

    @Column(name = "캐릭터명")
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(name = "캐릭터유형")
    private CharacterType characterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "직업유형")
    private JobType jobTypeSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "주거유형")
    private HousingType housingType;

    @Column(name = "현재턴", nullable = false)
    private Integer currentTurn;

    @Column(name = "경기사이클유형")
    private String economicCycleType;

    @Column(name = "현재날짜")
    private LocalDate currentDate;

    @Column(name = "보유현금", nullable = false)
    private Integer cash;

    @Column(name = "순자산")
    private Integer netAssets;

    @Column(name = "진행중여부", nullable = false)
    private boolean inProgress;

    @Column(name = "파산여부", nullable = false)
    private boolean bankrupt;

    @Column(name = "클리어여부", nullable = false)
    private boolean cleared;

    @Column(name = "게임생성일시", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "최종플레이일시")
    private LocalDateTime lastPlayedAt;

    @Column(name = "저장슬롯번호")
    private Integer saveSlotId;

    @Column(name = "목표지역코드")
    private String targetRegionCode;

    @Column(name = "목표구코드")
    private String targetDistrictCode;

    @Column(name = "시작데이터유형")
    private String seedType;

    @Column(name = "세션상태")
    private String sessionStatus;

    @Column(name = "보유부동산매물번호", nullable = false)
    private Integer ownedPropertyListingId;
}
