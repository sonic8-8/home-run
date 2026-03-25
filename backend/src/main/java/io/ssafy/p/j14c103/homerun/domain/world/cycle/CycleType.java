package io.ssafy.p.j14c103.homerun.domain.world.cycle;

public enum CycleType {
    CYCLE_BOOM(CyclePhase.BOOM, "초과 유동성과 투자 낙관이 이어지는 확장 국면", 24, 48),
    CYCLE_FINANCIAL_CRISIS(CyclePhase.CRISIS, "금융 시스템 불안이 실물경제 위기로 번지는 국면", 30, 30),
    CYCLE_CURRENCY_CRISIS(CyclePhase.CRISIS, "급격한 환율 불안과 자본 유출이 발생하는 국면", 18, 44),
    CYCLE_TECH_BUBBLE(CyclePhase.CRISIS, "기술주 과열 이후 밸류에이션 붕괴가 나타나는 국면", 24, 30),
    CYCLE_OIL_SHOCK(CyclePhase.CRISIS, "에너지 가격 급등으로 원가 압박이 심해지는 국면", 12, 24),
    CYCLE_STAGFLATION(CyclePhase.CRISIS, "고물가와 저성장이 동시에 이어지는 국면", 24, 36),
    CYCLE_PANDEMIC(CyclePhase.CRISIS, "보건 위기로 소비와 생산이 급격히 위축되는 국면", 6, 12),
    CYCLE_GREAT_DEPRESSION(CyclePhase.CRISIS, "장기 침체와 자산 디플레이션이 지속되는 국면", 36, 60),
    CYCLE_RATE_HIKE(CyclePhase.RECOVERY, "긴축 기조 속에서 경기 회복을 시험하는 국면", 18, 24),
    CYCLE_GEOPOLITICAL(CyclePhase.RECOVERY, "지정학 리스크 속에서 회복 흐름이 흔들리는 국면", 6, 18);

    private final CyclePhase phase;
    private final String description;
    private final int minDurationTurns;
    private final int maxDurationTurns;

    CycleType(
        final CyclePhase phase,
        final String description,
        final int minDurationTurns,
        final int maxDurationTurns
    ) {
        this.phase = phase;
        this.description = description;
        this.minDurationTurns = minDurationTurns;
        this.maxDurationTurns = maxDurationTurns;
    }

    public CyclePhase getPhase() {
        return phase;
    }

    public String getDescription() {
        return description;
    }

    public int getMinDurationTurns() {
        return minDurationTurns;
    }

    public int getMaxDurationTurns() {
        return maxDurationTurns;
    }
}
