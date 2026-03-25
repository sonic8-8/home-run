package io.ssafy.p.j14c103.homerun.api.service.financial;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.config.SseEmitterManager;
import io.ssafy.p.j14c103.homerun.domain.financial.UserInvestmentHoldingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 5분 주기로 보유 종목 현재가를 갱신하고, SSE로 대시보드를 푸시한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentPriceScheduler {

    private final UserInvestmentHoldingRepository holdingRepository;
    private final UserFinancialSummaryService summaryService;
    private final DashboardService dashboardService;
    private final SseEmitterManager sseEmitterManager;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void refreshAndPush() {
        final List<Long> userIds = holdingRepository.findDistinctUserIdsByActiveYnTrue();

        if (userIds.isEmpty()) {
            return;
        }

        log.info("주식 현재가 일괄 갱신 시작. 대상 사용자 수={}", userIds.size());

        for (final Long userId : userIds) {
            try {
                summaryService.getSummary(userId);

                if (sseEmitterManager.hasEmitter(userId)) {
                    final DashboardResponse dashboard = dashboardService.getDashboard(userId);
                    sseEmitterManager.send(userId, "dashboard-update", dashboard);
                }
            } catch (final Exception e) {
                log.warn("사용자 주식 가격 갱신 실패. userId={}", userId, e);
            }
        }

        log.info("주식 현재가 일괄 갱신 완료.");
    }
}
