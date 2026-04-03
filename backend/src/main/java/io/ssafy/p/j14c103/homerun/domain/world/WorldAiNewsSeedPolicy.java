package io.ssafy.p.j14c103.homerun.domain.world;

import java.util.List;

public class WorldAiNewsSeedPolicy {

    public List<AiNewsSeed> calculate() {
        return List.of(
            aiNews(
                "AI-NEWS-BOOM-BOOM-001",
                "수출 회복세가 이어지며 성장 기대가 더 커집니다.",
                "positive",
                "호머런 리서치",
                "기업 실적과 투자 심리가 모두 살아나면서 경기 확장 흐름이 이어지고 있습니다.",
                "BOOM_TO_BOOM",
                "호황이 다음 턴에도 유지되는 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-BOOM-CRISIS-001",
                "과열 신호가 누적되며 경기 급랭 우려가 커집니다.",
                "negative",
                "호머런 리서치",
                "높아진 가격 부담과 투자 피로가 누적되며 시장이 빠르게 식을 가능성이 커졌습니다.",
                "BOOM_TO_CRISIS",
                "호황 이후 위기 전환용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-BOOM-RECOVERY-001",
                "상승 탄력이 줄며 시장이 점진적 안정 국면으로 들어섭니다.",
                "mixed",
                "호머런 리서치",
                "과열 구간은 지나고 있으나 급락보다는 완만한 안정화가 예상됩니다.",
                "BOOM_TO_RECOVERY",
                "호황 이후 안정화 전환용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-CRISIS-CRISIS-001",
                "수요 위축이 길어지며 침체 국면이 이어집니다.",
                "negative",
                "호머런 리서치",
                "투자와 소비가 모두 둔화되며 위기 국면의 지속 가능성이 높게 평가됩니다.",
                "CRISIS_TO_CRISIS",
                "위기 지속용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-CRISIS-RECOVERY-001",
                "정책 완화 기대가 퍼지며 경기 반등 신호가 나타납니다.",
                "mixed",
                "호머런 리서치",
                "지원책과 심리 회복이 맞물리며 완만한 회복 흐름이 관측됩니다.",
                "CRISIS_TO_RECOVERY",
                "위기 이후 회복 전환용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-CRISIS-BOOM-001",
                "대규모 투자와 고용 회복이 겹치며 반등 폭이 예상보다 커집니다.",
                "positive",
                "호머런 리서치",
                "침체를 빠르게 벗어나 성장 기대가 커지면서 시장 분위기가 급격히 반전됐습니다.",
                "CRISIS_TO_BOOM",
                "위기 이후 호황 전환용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-RECOVERY-RECOVERY-001",
                "회복 흐름이 이어지며 경기 정상화가 점진적으로 진행됩니다.",
                "mixed",
                "호머런 리서치",
                "불확실성은 남아 있지만 회복 흐름 자체는 유지되는 것으로 해석됩니다.",
                "RECOVERY_TO_RECOVERY",
                "회복 유지용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-RECOVERY-CRISIS-001",
                "회복 기대가 꺾이며 다시 방어적 대응이 필요해졌습니다.",
                "negative",
                "호머런 리서치",
                "금리 부담과 소비 둔화가 겹치면서 회복 국면이 다시 흔들리고 있습니다.",
                "RECOVERY_TO_CRISIS",
                "회복 이후 재악화 전환용 기본 뉴스 seed"
            ),
            aiNews(
                "AI-NEWS-RECOVERY-BOOM-001",
                "기업 투자와 소비가 함께 살아나며 확장 국면 진입이 가시화됩니다.",
                "positive",
                "호머런 리서치",
                "고용과 자산시장이 동시에 개선되며 회복을 넘어 성장 국면 진입 가능성이 커졌습니다.",
                "RECOVERY_TO_BOOM",
                "회복 이후 호황 전환용 기본 뉴스 seed"
            )
        );
    }

    private AiNewsSeed aiNews(
        final String newsId,
        final String title,
        final String sentiment,
        final String sourceName,
        final String articleText,
        final String economicCycleType,
        final String reason
    ) {
        return new AiNewsSeed(
            newsId,
            title,
            sentiment,
            sourceName,
            articleText,
            economicCycleType,
            reason
        );
    }

    public record AiNewsSeed(
        String newsId,
        String title,
        String sentiment,
        String sourceName,
        String articleText,
        String economicCycleType,
        String reason
    ) {
    }
}
