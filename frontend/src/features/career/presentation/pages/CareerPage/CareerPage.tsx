import clsx from 'clsx';
import { Link } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { PageSpinner } from '@shared/components/PageSpinner';
import { formatMoney } from '@shared/utils/formatter';
import type { CareerJobType } from '../../../domain/entities/CareerSession';
import type { JobOffer } from '../../../domain/entities/JobOffer';
import { useCareerPage } from '../../hooks/useCareerPage';
import styles from './CareerPage.module.css';

const JOB_TYPE_LABELS: Record<CareerJobType, string> = {
  LARGE_BIZ: '대기업 직장인',
  MID_BIZ: '중견기업 직장인',
  SMALL_BIZ: '중소기업 직장인',
  STARTUP: '스타트업 직장인',
  FREELANCER: '프리랜서',
};

export function CareerPage() {
  const {
    activeSession,
    jobOfferList,
    currentSalary,
    salaryNegotiationResult,
    transferResult,
    isSessionLoading,
    isOffersLoading,
    isNegotiating,
    transferringOfferId,
    pageError,
    offerError,
    actionError,
    refresh,
    negotiateSalary,
    transferJob,
  } = useCareerPage();

  const hasActiveSession = activeSession !== null;
  const offers = jobOfferList?.offers ?? [];

  return (
    <main className={styles.page} data-testid="career-page">
      <section className={styles.hero}>
        <div className={styles.heroBody}>
          <span className={styles.eyebrow}>Career Center</span>
          <h1 className={styles.title}>커리어 협상과 이직 관리</h1>
          <p className={styles.description}>
            진행 중 세션을 기준으로 연봉 협상과 이직 제안을 확인하고 바로 반영합니다.
          </p>
        </div>
        <button
          type="button"
          className={styles.primaryButton}
          onClick={() => void refresh()}
          disabled={isSessionLoading}
        >
          새로고침
        </button>
      </section>

      {pageError && (
        <div className={styles.errorBanner} role="alert">
          <span>{pageError}</span>
          <button
            type="button"
            className={styles.inlineButton}
            onClick={() => void refresh()}
            disabled={isSessionLoading}
          >
            다시 시도
          </button>
        </div>
      )}

      {actionError && (
        <div className={styles.errorBanner} role="alert">
          <span>{actionError}</span>
        </div>
      )}

      {isSessionLoading && !hasActiveSession ? (
        <div className={styles.stateCard} data-testid="career-loading-state">
          <PageSpinner />
        </div>
      ) : !hasActiveSession ? (
        <section className={styles.stateCard} data-testid="career-empty-state">
          <strong className={styles.stateTitle}>진행 중인 게임 세션이 없습니다.</strong>
          <p className={styles.stateText}>
            커리어 협상과 이직은 진행 중 세션이 있을 때만 이용할 수 있습니다.
          </p>
          <div className={styles.linkRow}>
            <Link className={styles.secondaryLink} to={ROUTES.GAME_SAVE_WITH_MODE('continue')}>
              저장 슬롯 확인
            </Link>
            <Link className={styles.secondaryLink} to={ROUTES.GAME_START}>
              새 게임 시작
            </Link>
          </div>
        </section>
      ) : (
        <>
          <section className={styles.summaryGrid}>
            <article className={styles.summaryCard} data-testid="career-session-card">
              <span className={styles.cardLabel}>활성 세션</span>
              <strong className={styles.cardValue}>
                {activeSession.characterName ?? '플레이어'}
              </strong>
              <div className={styles.metaGrid}>
                <div>
                  <span className={styles.metaLabel}>세션</span>
                  <strong>{activeSession.sessionId}</strong>
                </div>
                <div>
                  <span className={styles.metaLabel}>슬롯</span>
                  <strong>{activeSession.slotNumber}번</strong>
                </div>
                <div>
                  <span className={styles.metaLabel}>현재 직군</span>
                  <strong>{formatJobType(activeSession.jobType)}</strong>
                </div>
                <div>
                  <span className={styles.metaLabel}>진행 턴</span>
                  <strong>{activeSession.currentTurn ?? '-'}턴</strong>
                </div>
              </div>
            </article>

            <article className={clsx(styles.summaryCard, styles.salaryCard)}>
              <span className={styles.cardLabel}>현재 기준 연봉</span>
              <strong className={styles.salaryValue}>
                {currentSalary === null ? '확인 필요' : `${formatMoney(currentSalary)}원`}
              </strong>
              <p className={styles.cardText}>
                연봉 협상이나 이직 제안을 반영하면 최신 금액으로 갱신됩니다.
              </p>
            </article>
          </section>

          <section className={styles.contentGrid}>
            <article className={styles.panel}>
              <header className={styles.panelHeader}>
                <div>
                  <span className={styles.panelEyebrow}>Salary Negotiation</span>
                  <h2 className={styles.panelTitle}>연봉 협상</h2>
                </div>
                <button
                  type="button"
                  className={styles.primaryButton}
                  onClick={() => void negotiateSalary()}
                  disabled={isNegotiating}
                >
                  {isNegotiating ? '협상 처리 중...' : '연봉 협상 진행'}
                </button>
              </header>
              <p className={styles.panelDescription}>
                현재 세션의 커리어 상태를 기준으로 연봉 협상 결과를 즉시 반영합니다.
              </p>

              {salaryNegotiationResult === null ? (
                <div className={styles.placeholderCard}>
                  <strong className={styles.stateTitle}>아직 연봉 협상을 진행하지 않았습니다.</strong>
                  <p className={styles.stateText}>
                    협상 버튼을 누르면 백엔드 결과와 메시지를 그대로 확인할 수 있습니다.
                  </p>
                </div>
              ) : (
                <div
                  className={clsx(
                    styles.resultCard,
                    salaryNegotiationResult.success ? styles.resultSuccess : styles.resultMuted,
                  )}
                  data-testid="career-negotiation-result"
                >
                  <div className={styles.resultHeader}>
                    <strong>
                      {salaryNegotiationResult.success ? '협상 반영 완료' : '협상 결과 확인'}
                    </strong>
                    <span className={styles.resultBadge}>
                      {salaryNegotiationResult.raiseRate}% 인상
                    </span>
                  </div>
                  <p className={styles.resultMessage}>{salaryNegotiationResult.message}</p>
                  <dl className={styles.resultGrid}>
                    <div>
                      <dt>이전 연봉</dt>
                      <dd>{formatMoney(salaryNegotiationResult.previousSalary)}원</dd>
                    </div>
                    <div>
                      <dt>협상 후 연봉</dt>
                      <dd>{formatMoney(salaryNegotiationResult.newSalary)}원</dd>
                    </div>
                    <div>
                      <dt>마지막 협상 턴</dt>
                      <dd>{salaryNegotiationResult.lastNegotiatedTurn}턴</dd>
                    </div>
                  </dl>
                </div>
              )}

              {transferResult !== null && (
                <div className={styles.transferSummary} data-testid="career-transfer-result">
                  <strong>{transferResult.newJobTitle}</strong>
                  <p>{transferResult.message}</p>
                  <p>
                    새 연봉 {formatMoney(transferResult.newSalary)}원
                    {transferResult.probationEndTurn === null
                      ? ''
                      : ` · 수습 종료 ${transferResult.probationEndTurn}턴`}
                  </p>
                </div>
              )}
            </article>

            <article className={styles.panel}>
              <header className={styles.panelHeader}>
                <div>
                  <span className={styles.panelEyebrow}>Job Offers</span>
                  <h2 className={styles.panelTitle}>이직 제안</h2>
                </div>
                <button
                  type="button"
                  className={styles.secondaryButton}
                  onClick={() => void refresh()}
                  disabled={isOffersLoading || isSessionLoading}
                >
                  제안 다시 조회
                </button>
              </header>

              {jobOfferList !== null && (
                <div className={styles.badgeRow}>
                  <span className={styles.infoBadge}>
                    제안 보너스 {jobOfferList.offerChanceBonusRate}%
                  </span>
                  {jobOfferList.meetFriendBonusApplied && (
                    <span className={styles.infoBadge}>인맥 보너스 적용됨</span>
                  )}
                </div>
              )}

              {offerError && (
                <div className={styles.errorBanner} role="alert">
                  <span>{offerError}</span>
                </div>
              )}

              {isOffersLoading && offers.length === 0 ? (
                <div className={styles.stateCard} data-testid="career-offers-loading-state">
                  <PageSpinner />
                </div>
              ) : offers.length === 0 ? (
                <div className={styles.stateCard} data-testid="career-offers-empty-state">
                  <strong className={styles.stateTitle}>받은 이직 제안이 없습니다.</strong>
                  <p className={styles.stateText}>
                    이번 턴 조건이 충족되면 이 영역에 회사별 제안이 표시됩니다.
                  </p>
                </div>
              ) : (
                <div className={styles.offerList}>
                  {offers.map((offer) => (
                    <OfferCard
                      key={offer.offerId}
                      offer={offer}
                      isSubmitting={transferringOfferId === offer.offerId}
                      onTransfer={() => void transferJob(offer.offerId)}
                    />
                  ))}
                </div>
              )}
            </article>
          </section>
        </>
      )}
    </main>
  );
}

interface OfferCardProps {
  offer: JobOffer;
  isSubmitting: boolean;
  onTransfer: () => void;
}

function OfferCard({
  offer,
  isSubmitting,
  onTransfer,
}: OfferCardProps) {
  const salaryDelta = offer.offeredSalary - offer.currentSalary;

  return (
    <article
      className={styles.offerCard}
      data-testid={`career-offer-${offer.offerId}`}
    >
      <div className={styles.offerHeader}>
        <div>
          <span className={styles.offerCompany}>{offer.companyName}</span>
          <h3 className={styles.offerTitle}>{formatJobType(offer.jobType)}</h3>
        </div>
        <span className={styles.offerDelta}>
          {salaryDelta >= 0 ? '+' : '-'}
          {formatMoney(Math.abs(salaryDelta))}원
        </span>
      </div>

      <dl className={styles.offerMeta}>
        <div>
          <dt>현재 연봉</dt>
          <dd>{formatMoney(offer.currentSalary)}원</dd>
        </div>
        <div>
          <dt>제안 연봉</dt>
          <dd>{formatMoney(offer.offeredSalary)}원</dd>
        </div>
        <div>
          <dt>수습 기간</dt>
          <dd>{offer.probationTurns === null ? '없음' : `${offer.probationTurns}턴`}</dd>
        </div>
      </dl>

      <button
        type="button"
        className={styles.primaryButton}
        onClick={onTransfer}
        disabled={isSubmitting}
      >
        {isSubmitting ? '이직 반영 중...' : '이직 수락'}
      </button>
    </article>
  );
}

function formatJobType(jobType: CareerJobType | null): string {
  if (jobType === null) {
    return '직업 정보 없음';
  }

  return JOB_TYPE_LABELS[jobType];
}
