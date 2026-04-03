import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';
import { GameEventFlowModal } from '@features/game/presentation/components/GameEventFlowModal';
import { NewsEventModal } from '@features/game/presentation/components/NewsEventModal/NewsEventModal';
import { MonthlyActivityModal } from '@features/game/presentation/components/MonthlyActivityModal/MonthlyActivityModal';
import { LoanProductsPanel } from '@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel';
import { CardRecommendPanel } from '@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel';
import { StockTradingPanel } from '@features/game/presentation/components/StockTradingPanel';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import { formatKoreanDate, formatMoney } from '@shared/utils/formatter';
import sceneRoad from '@assets/images/game_back_road.png';
import styles from './GameMainPage.module.css';

const CHARACTER_IMAGE: Record<CharacterType, string> = {
  MALE: '/assets/images/bcharac.png',
  FEMALE: '/assets/images/gcharac.png',
};

const STAT_LABELS = {
  health: '체력',
  fatigue: '피로',
  stress: '스트레스',
  happiness: '행복',
  knowledge: '지식',
} as const;

function GameMainPageContent() {
  const navigate = useNavigate();
  const { getTriggerLabel, toggleFlow } = useGameGuide();
  const {
    sessionId,
    turn,
    currentDate,
    characterType,
    news,
    currentPendingEvent,
    resolvedEvent,
    hasMorePendingEvents,
    turnActions,
    turnPreview,
    turnCommitResult,
    isNewsLoading,
    newsError,
    isNewsOpen,
    isMonthlyActivityOpen,
    isGameEventOpen,
    isActionsLoading,
    isSlotSubmitting,
    isTurnCommitting,
    isPendingEventsLoading,
    isEventResolving,
    scheduleError,
    eventError,
    openNews,
    closeNews,
    openMonthlyActivity,
    closeMonthlyActivity,
    closeGameEvent,
    submitTurnSlots,
    commitTurn,
    confirmCommitResult,
    resolveGameEvent,
    advanceGameEvent,
    resetScheduleFlow,
    error,
    isLoading,
    leftView,
    setLeftView,
    confirmedLoan,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  } = useGameMain();

  const characterImage = CHARACTER_IMAGE[characterType];
  const runtimeSnapshot = turn?.runtimeSnapshot ?? null;

  if (isLoading && currentDate === null) {
    return (
      <div className={styles.page}>
        <div className={styles.container}>게임 정보를 불러오는 중입니다.</div>
      </div>
    );
  }

  if (error && currentDate === null) {
    return (
      <div className={styles.page}>
        <div className={styles.container}>{error}</div>
      </div>
    );
  }

  return (
    <>
      <div className={styles.page}>
        <div className={styles.container}>
          {leftView === 'scene' ? (
            <div
              className={styles.scene}
              style={{ backgroundImage: `url(${sceneRoad})` }}
            >
              <div className={styles.dateLabel}>
                <span className={styles.calendarIcon}>📅</span>
                {currentDate === null ? '' : formatKoreanDate(currentDate)}
              </div>
              <img src={characterImage} alt="캐릭터" className={styles.character} />
            </div>
          ) : (
            <div className={styles.leftPanel}>
              {leftView === 'loan' && (
                sessionId !== null ? (
                  <LoanProductsPanel
                    key={`loan-panel-${sessionId}-${confirmedLoan?.loanId ?? 'stored'}`}
                    sessionId={sessionId}
                    confirmedLoan={confirmedLoan}
                    preSelectedPropertyId={preSelectedPropertyId}
                    preSelectedPropertyName={preSelectedPropertyName}
                    preSelectedPropertyPrice={preSelectedPropertyPrice}
                  />
                ) : null
              )}
              {leftView === 'card' && <CardRecommendPanel />}
              {leftView === 'stock' && (
                sessionId !== null ? <StockTradingPanel sessionId={sessionId} /> : null
              )}
            </div>
          )}

          <div className={styles.panel}>
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>현재 상태</h2>
              {runtimeSnapshot === null ? (
                <div className={styles.inlineError}>현재 상태를 불러오는 중입니다.</div>
              ) : (
                <>
                  <div className={styles.assetList}>
                    <div className={styles.assetRow}>
                      <span className={styles.assetLabelBold}>현금</span>
                      <span className={styles.assetValueBold}>
                        {formatMoney(runtimeSnapshot.assets.cashBalance)} 원
                      </span>
                    </div>
                    <div className={styles.assetRow}>
                      <span className={styles.assetLabelBold}>순자산</span>
                      <span className={styles.assetValueBold}>
                        {formatMoney(runtimeSnapshot.assets.netWorth)} 원
                      </span>
                    </div>
                  </div>

                  <div className={styles.statList}>
                    {Object.entries(runtimeSnapshot.stats).map(([key, value]) => (
                      <div key={key} className={styles.statRow}>
                        <span className={styles.statLabel}>
                          {STAT_LABELS[key as keyof typeof STAT_LABELS]}
                        </span>
                        <div className={styles.statBarTrack}>
                          <div
                            className={styles.statBarFill}
                            style={{ width: `${Math.max(0, Math.min(value, 100))}%` }}
                          />
                        </div>
                        <span className={styles.statValue}>{value}</span>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </section>

            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>이번 달 진행</h2>
              <div className={styles.turnCard}>
                <div className={styles.turnCardLabel}>턴</div>
                <div className={styles.turnCardValue}>
                  {turn === null ? '-' : `${turn.turnNumber}번째 달`}
                </div>
                <div className={styles.turnCardMeta}>
                  {turn === null
                    ? '경제 흐름을 불러오는 중입니다.'
                    : `${turn.economicCycle.description} · ${formatKoreanDate(turn.currentDate)}`}
                </div>
              </div>
              <button
                className={styles.actionButton}
                onClick={openMonthlyActivity}
                disabled={sessionId === null}
                data-guide="game-turn-action"
              >
                이번 달 활동 진행
              </button>
              {scheduleError && !isMonthlyActivityOpen && (
                <div className={styles.inlineError}>{scheduleError}</div>
              )}
            </section>

            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>메뉴</h2>
              <div className={styles.menuList}>
                <button
                  className={leftView === 'loan' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'loan' ? 'scene' : 'loan')}
                  data-guide="game-loan"
                >
                  대출/상환
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => {
                    if (sessionId === null) {
                      return;
                    }

                    navigate(ROUTES.REAL_ESTATE, {
                      state: { sessionId, mode: 'browse' },
                    });
                  }}
                  data-guide="game-property"
                >
                  부동산 알아보기
                </button>
                <button
                  className={leftView === 'card' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'card' ? 'scene' : 'card')}
                  data-guide="game-card"
                >
                  카드 추천
                </button>
                <button
                  className={leftView === 'stock' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'stock' ? 'scene' : 'stock')}
                  data-guide="game-stock-launch"
                >
                  주식 투자
                </button>
                <button
                  className={styles.menuButton}
                  onClick={openNews}
                  data-guide="game-news"
                >
                  이달의 뉴스
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => toggleFlow('main')}
                >
                  {getTriggerLabel('main')}
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => navigate(ROUTES.HOME)}
                >
                  금융 홈으로
                </button>
              </div>
            </section>
          </div>
        </div>
      </div>

      {sessionId !== null && (
        <NewsEventModal
          isOpen={isNewsOpen}
          onClose={closeNews}
          news={news}
          loading={isNewsLoading}
          error={newsError}
          onOpenArchive={() => {
            navigate(ROUTES.GAME_NEWS(sessionId));
          }}
        />
      )}

      {sessionId !== null && (
        <MonthlyActivityModal
          isOpen={isMonthlyActivityOpen}
          month={turn?.month ?? (currentDate === null ? 0 : currentDate.getMonth() + 1)}
          shopping={turnActions?.shopping ?? []}
          activities={turnActions?.activities ?? []}
          preview={turnPreview}
          commitResult={turnCommitResult}
          isActionsLoading={isActionsLoading}
          isSubmitting={isSlotSubmitting}
          isCommitting={isTurnCommitting}
          error={scheduleError ?? eventError}
          onClose={closeMonthlyActivity}
          onSubmitSlots={(actionTypes) => {
            void submitTurnSlots(actionTypes)
          }}
          onBackToSelection={resetScheduleFlow}
          onCommit={() => {
            void commitTurn()
          }}
          onConfirmCommitResult={() => {
            void confirmCommitResult()
          }}
          isResultConfirming={isPendingEventsLoading}
        />
      )}

      <GameEventFlowModal
        isOpen={isGameEventOpen}
        event={currentPendingEvent}
        resolvedEvent={resolvedEvent}
        hasMoreEvents={hasMorePendingEvents}
        isLoading={isPendingEventsLoading}
        isResolving={isEventResolving}
        error={eventError}
        onResolve={(choiceId) => {
          void resolveGameEvent(choiceId)
        }}
        onRetry={() => {
          void confirmCommitResult()
        }}
        onContinue={advanceGameEvent}
        onClose={closeGameEvent}
      />
    </>
  );
}

export function GameMainPage() {
  const location = useLocation();

  return <GameMainPageContent key={location.key} />;
}
