import { useRef, useState, type CSSProperties } from 'react';
import maleClearCharacterImage from '@assets/images/result/bcharac_clear.png';
import maleBankruptCharacterImage from '@assets/images/result/bcharac_bankrupt.png';
import maleTimeoutCharacterImage from '@assets/images/result/bcharac_timeout.png';
import maleForeclosureCharacterImage from '@assets/images/result/bcharac_foreclosed.png';
import clearCharacterImage from '@assets/images/result/gcharac_clear.png';
import bankruptCharacterImage from '@assets/images/result/gcharac_bankrupt.png';
import timeoutCharacterImage from '@assets/images/result/gcharac_timeout.png';
import foreclosureCharacterImage from '@assets/images/result/gcharac_foreclosed.png';
import reportBackground from '@assets/images/background.png';
import roadBackground from '@assets/images/game_back_road.png';
import apartmentBackground from '@assets/images/gameback_aprt.png';
import villaBackground from '@assets/images/gameback_villa.png';
import homerunLogo from '@assets/images/logo.png';
import { formatIsoDate } from '@shared/utils/formatter';
import { useEndingPage } from '@features/ending/presentation/hooks/useEndingPage';
import type { CharacterType, EndingType, HousingType } from '@features/ending/domain/entities/EndingReport';
import styles from './EndingPage.module.css';

const femaleEndingIllustrationMap: Partial<Record<EndingType, string>> = {
  CLEAR: clearCharacterImage,
  BANKRUPT: bankruptCharacterImage,
  TIMEOUT: timeoutCharacterImage,
  FORECLOSURE: foreclosureCharacterImage,
};

const maleEndingIllustrationMap: Partial<Record<EndingType, string>> = {
  CLEAR: maleClearCharacterImage,
  BANKRUPT: maleBankruptCharacterImage,
  TIMEOUT: maleTimeoutCharacterImage,
  FORECLOSURE: maleForeclosureCharacterImage,
};

function formatNumber(value: number | null): string {
  if (value === null) {
    return '-';
  }

  return new Intl.NumberFormat('ko-KR').format(value);
}

function formatPercent(value: number | null): string {
  if (value === null) {
    return '-';
  }

  return `${Math.round(value * 100)}%`;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

function toEndingTypeLabel(endingType: EndingType): string {
  switch (endingType) {
    case 'CLEAR':
      return '목표 달성';
    case 'BANKRUPT':
      return '파산';
    case 'TIMEOUT':
      return '기한 초과';
    case 'FORECLOSURE':
      return '차압';
    case 'IN_PROGRESS':
    default:
      return '진행 중';
  }
}

function getReportLead(endingType: EndingType): string {
  switch (endingType) {
    case 'CLEAR':
      return '목표를 이루기까지의 선택과 축적이 한 장의 기록으로 남았습니다.';
    case 'BANKRUPT':
      return '무너진 순간까지의 소비와 선택이 다음 도전을 위한 데이터가 됩니다.';
    case 'TIMEOUT':
      return '끝내 도달하지 못했어도, 남긴 발자국은 분명한 성장의 흔적입니다.';
    case 'FORECLOSURE':
      return '차압 직전까지의 흔들림과 상실의 기록을 시간순으로 되짚습니다.';
    case 'IN_PROGRESS':
    default:
      return '현재까지 쌓인 결과와 기록을 정리합니다.';
  }
}

function getVerdictText(endingType: EndingType): string {
  switch (endingType) {
    case 'CLEAR':
      return '꾸준히 버티고 기회를 잡아 결국 목표를 달성했습니다. 이번 회차의 선택과 기록은 가장 안정적인 흐름으로 남았습니다.';
    case 'BANKRUPT':
      return '지출과 위기를 끝내 감당하지 못했지만, 어느 선택이 흐름을 흔들었는지 이번 기록에서 분명히 확인할 수 있습니다.';
    case 'TIMEOUT':
      return '목표에 도달하진 못했어도 자산과 선택의 흐름은 분명한 발자국으로 남았습니다. 다음 회차의 전략을 세우기엔 충분한 기록입니다.';
    case 'FORECLOSURE':
      return '장기 연체와 주거 상실로 엔딩에 도달했습니다. 어떤 위기가 겹쳤는지 엔딩 리포트에서 차근히 되짚어볼 수 있습니다.';
    case 'IN_PROGRESS':
    default:
      return '현재까지의 기록을 기준으로 다음 선택을 준비할 수 있습니다.';
  }
}

function toHousingTypeLabel(housingType: HousingType | null): string {
  switch (housingType) {
    case 'NONE':
      return '주거 없음';
    case 'STUDIO':
      return '원룸';
    case 'VILLA':
      return '빌라';
    case 'JEONSE_APT':
      return '전세 아파트';
    case 'OWNED_APT':
      return '자가';
    default:
      return '-';
  }
}

function getGradeClassName(grade: string): string {
  switch (grade) {
    case 'S':
      return styles.gradeS;
    case 'A':
      return styles.gradeA;
    case 'B':
      return styles.gradeB;
    case 'C':
      return styles.gradeC;
    default:
      return styles.gradeDefault;
  }
}

function getSceneBackground(endingType: EndingType, housingType: HousingType | null): string {
  if (endingType === 'BANKRUPT' || endingType === 'FORECLOSURE') {
    return roadBackground;
  }

  if (endingType === 'TIMEOUT') {
    return villaBackground;
  }

  if (housingType === 'OWNED_APT') {
    return apartmentBackground;
  }

  if (housingType === 'JEONSE_APT' || housingType === 'VILLA' || housingType === 'STUDIO') {
    return villaBackground;
  }

  return roadBackground;
}

function getEndingNarrative(endingType: EndingType): string {
  switch (endingType) {
    case 'CLEAR':
      return '긴 시간을 버틴 끝에, 당신은 결국 목표했던 삶에 도착했습니다.';
    case 'BANKRUPT':
      return '돈은 바닥났지만, 이 선택들의 기록은 다음 도전을 위한 근거가 됩니다.';
    case 'TIMEOUT':
      return '목표에 닿지는 못했지만, 이번 생의 결과는 분명한 흔적으로 남았습니다.';
    case 'FORECLOSURE':
      return '차압으로 마지막 집을 잃었고, 이번 생은 뼈아픈 기록으로 끝났습니다.';
    case 'IN_PROGRESS':
    default:
      return '아직 이야기는 끝나지 않았습니다.';
  }
}

function getEndingIllustration(
  characterType: CharacterType,
  endingType: EndingType,
): string | null {
  const illustrationMap =
    characterType === 'FEMALE'
      ? femaleEndingIllustrationMap
      : maleEndingIllustrationMap;

  return illustrationMap[endingType] ?? null;
}

function formatHousingState(state: { housingType: HousingType; propertyId: number | null } | null): string {
  if (state === null) {
    return '기록 없음';
  }

  return toHousingTypeLabel(state.housingType);
}

function buildLinePoints(values: readonly number[], width: number, height: number, padding: number): string {
  if (values.length === 0) {
    return '';
  }

  const maxValue = Math.max(...values, 1);
  const minValue = Math.min(...values, 0);
  const range = Math.max(maxValue - minValue, 1);
  const innerWidth = Math.max(width - padding * 2, 1);
  const innerHeight = Math.max(height - padding * 2, 1);

  return values
    .map((value, index) => {
      const x = padding + (values.length === 1 ? innerWidth / 2 : (innerWidth * index) / (values.length - 1));
      const normalized = (value - minValue) / range;
      const y = height - padding - normalized * innerHeight;

      return `${x},${y}`;
    })
    .join(' ');
}

function buildAreaPath(values: readonly number[], width: number, height: number, padding: number): string {
  const points = buildLinePoints(values, width, height, padding);

  if (points.length === 0) {
    return '';
  }

  const pointEntries = points.split(' ');
  const first = pointEntries[0];
  const last = pointEntries[pointEntries.length - 1];
  const [lastX] = last.split(',');

  return `M ${first} L ${pointEntries.slice(1).join(' L ')} L ${lastX},${height - padding} L ${first.split(',')[0]},${height - padding} Z`;
}

function createReportBackgroundStyle(backgroundImage: string): CSSProperties {
  return {
    '--report-background-image': `url(${backgroundImage})`,
  } as CSSProperties;
}

export function EndingPage() {
  const {
    report,
    timeline,
    isLoading,
    error,
    isNotReady,
    hasValidSessionId,
  } = useEndingPage();
  const [isReportVisible, setIsReportVisible] = useState(false);
  const reportRef = useRef<HTMLElement | null>(null);

  if (!hasValidSessionId) {
    return null;
  }

  if (isLoading) {
    return (
      <main className={styles.page}>
        <div className={styles.status}>엔딩 정보를 불러오는 중...</div>
      </main>
    );
  }

  if (isNotReady) {
    return (
      <main className={styles.page}>
        <section className={styles.panel} data-testid="ending-not-ready">
          <h1 className={styles.title}>엔딩 리포트 준비 중</h1>
          <p className={styles.description}>
            세션이 아직 종료되지 않았거나 엔딩 리포트가 생성되지 않았습니다.
          </p>
        </section>
      </main>
    );
  }

  if (error !== null) {
    return (
      <main className={styles.page}>
        <div className={`${styles.status} ${styles.error}`}>{error}</div>
      </main>
    );
  }

  if (report === null) {
    return (
      <main className={styles.page}>
        <div className={styles.status}>엔딩 데이터를 찾을 수 없습니다.</div>
      </main>
    );
  }

  const illustration = getEndingIllustration(report.characterType, report.endingType);
  const sceneBackground = getSceneBackground(
    report.endingType,
    report.housingSnapshot?.currentHousingType ?? null,
  );
  const narrative = getEndingNarrative(report.endingType);
  const endingTypeLabel = toEndingTypeLabel(report.endingType);
  const reportLead = getReportLead(report.endingType);
  const verdictText = getVerdictText(report.endingType);
  const currentHousingLabel = toHousingTypeLabel(report.housingSnapshot?.currentHousingType ?? null);
  const gradeClassName = getGradeClassName(report.grade);
  const chartWidth = 640;
  const chartHeight = 220;
  const chartPadding = 24;
  const totalAssetSeries = timeline.map((item) => item.totalAssets ?? 0);
  const netAssetSeries = timeline.map((item) => item.netAssets ?? 0);
  const totalAssetLinePoints = buildLinePoints(totalAssetSeries, chartWidth, chartHeight, chartPadding);
  const netAssetLinePoints = buildLinePoints(netAssetSeries, chartWidth, chartHeight, chartPadding);
  const totalAssetAreaPath = buildAreaPath(totalAssetSeries, chartWidth, chartHeight, chartPadding);
  const topCategoryRatio = clamp(report.spendingPattern?.topCategoryRatio ?? 0, 0, 1);
  const hasReachedTargetProperty =
    report.housingSnapshot?.currentPropertyId != null &&
    report.housingSnapshot?.targetPropertyId != null &&
    report.housingSnapshot.currentPropertyId === report.housingSnapshot.targetPropertyId;

  const handleRevealReport = () => {
    setIsReportVisible(true);
  };

  if (!isReportVisible) {
    return (
      <main className={`${styles.page} ${styles.pageSceneOnly}`} data-testid="ending-page">
        <section
          className={styles.scene}
          data-testid="ending-scene"
          style={{ backgroundImage: `linear-gradient(180deg, rgba(9, 16, 24, 0.18), rgba(9, 16, 24, 0.72)), url(${sceneBackground})` }}
        >
          <img src={homerunLogo} alt="홈런" className={styles.sceneLogoCorner} />
          <div className={styles.sceneOverlay}>
            <div className={styles.sceneEyebrow}>ENDING</div>
            <h1 className={styles.sceneTitle}>{report.title}</h1>
            <p className={styles.sceneEndingType}>
              {report.endingType} · <span className={gradeClassName}>등급 {report.grade}</span>
            </p>
            <p className={styles.sceneNarrative}>{narrative}</p>

            <div className={styles.sceneMeta}>
              <span>현재 주거 {currentHousingLabel}</span>
              <span>총자산 {formatNumber(report.totalAssets)}</span>
            </div>

            <button
              type="button"
              className={styles.sceneCta}
              data-testid="ending-reveal-report"
              onClick={handleRevealReport}
            >
              {'>> 결과 보기'}
            </button>
          </div>

          <div className={styles.sceneVisual}>
            {illustration === null ? (
              <div className={styles.fallbackVisual}>{report.endingType}</div>
            ) : (
              <img
                className={styles.resultImage}
                src={illustration}
                alt={`${report.endingType} 엔딩 대표 이미지`}
              />
            )}
          </div>
        </section>
      </main>
    );
  }

  return (
    <main
      className={`${styles.page} ${styles.pageReportVisible}`}
      data-testid="ending-page"
      style={createReportBackgroundStyle(reportBackground)}
    >
      <section
        ref={reportRef}
        className={styles.reportPage}
        data-testid="ending-report"
      >
        <header className={styles.reportTopBar}>
          <button type="button" className={styles.reportBack} onClick={() => setIsReportVisible(false)}>
            엔딩 화면으로
          </button>
          <img src={homerunLogo} alt="홈런" className={styles.reportLogo} />
        </header>

        <section className={styles.reportHero} data-testid="ending-summary">
          <div className={styles.reportGradeBubble}>
            <span>등급</span>
            <strong className={gradeClassName}>{report.grade}</strong>
          </div>
          <h1 className={styles.reportTitle}>{report.title}</h1>
          <p className={styles.reportLead}>{reportLead}</p>
          <div className={styles.reportMeta}>
            <span>{endingTypeLabel}</span>
            <span>현재 주거 {currentHousingLabel}</span>
            <span>총자산 {formatNumber(report.totalAssets)}</span>
          </div>
        </section>

        <section className={styles.reportSection}>
          <div className={styles.reportSectionHeader}>
            <h2 className={styles.reportSectionTitle}>이번 생의 결과</h2>
          </div>
          <div className={styles.storyCard}>
            <div className={styles.statGrid}>
              <article className={styles.statCard}>
                <span className={styles.statLabel}>총 자산</span>
                <strong className={styles.statValue}>{formatNumber(report.totalAssets)}</strong>
              </article>
              <article className={styles.statCard}>
                <span className={styles.statLabel}>순이익</span>
                <strong className={styles.statValue}>{formatNumber(report.netProfit)}</strong>
              </article>
              <article className={styles.statCard}>
                <span className={styles.statLabel}>지출 성향</span>
                <strong className={styles.statValue}>
                  {report.spendingPattern?.topCategory ?? '기록 없음'} {formatPercent(report.spendingPattern?.topCategoryRatio ?? null)}
                </strong>
              </article>
            </div>
            <div className={styles.storyDivider} />
            <p className={styles.verdictText}>{verdictText}</p>
            {report.achievements.length > 0 && (
              <div className={styles.badgeRow}>
                {report.achievements.slice(0, 3).map((achievement) => (
                  <span key={achievement.name} className={styles.resultBadge}>
                    {achievement.name}
                  </span>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className={styles.reportSection}>
          <div className={styles.reportSectionHeader}>
            <h2 className={styles.reportSectionTitle}>돈의 흐름</h2>
          </div>
          <div className={styles.financeGrid}>
            <div className={styles.reportCard} data-testid="ending-timeline">
              <p className={styles.reportSectionSub}>총 {timeline.length}개의 월별 로그가 준비되었습니다.</p>
              {timeline.length > 0 ? (
                <>
                  <div className={styles.timelineLegend}>
                    <span className={styles.timelineLegendItem}>
                      <span className={`${styles.timelineLegendDot} ${styles.timelineLegendDotPrimary}`} />
                      총자산
                    </span>
                    <span className={styles.timelineLegendItem}>
                      <span className={`${styles.timelineLegendDot} ${styles.timelineLegendDotSecondary}`} />
                      순자산
                    </span>
                  </div>
                  <div className={styles.timelineChartWrap}>
                    <svg
                      className={styles.timelineChart}
                      viewBox={`0 0 ${chartWidth} ${chartHeight}`}
                      role="img"
                      aria-label="자산 타임라인 그래프"
                    >
                      <defs>
                        <linearGradient id="ending-total-assets-fill" x1="0" x2="0" y1="0" y2="1">
                          <stop offset="0%" stopColor="rgba(87, 173, 255, 0.28)" />
                          <stop offset="100%" stopColor="rgba(87, 173, 255, 0.03)" />
                        </linearGradient>
                      </defs>
                      <line
                        x1={chartPadding}
                        y1={chartHeight - chartPadding}
                        x2={chartWidth - chartPadding}
                        y2={chartHeight - chartPadding}
                        className={styles.timelineAxis}
                      />
                      <line
                        x1={chartPadding}
                        y1={chartPadding}
                        x2={chartPadding}
                        y2={chartHeight - chartPadding}
                        className={styles.timelineAxis}
                      />
                      {totalAssetAreaPath !== '' && (
                        <path d={totalAssetAreaPath} className={styles.timelineArea} />
                      )}
                      {totalAssetLinePoints !== '' && (
                        totalAssetSeries.length === 1 ? (
                          <circle
                            cx={chartWidth / 2}
                            cy={chartHeight / 2}
                            r="7"
                            className={styles.timelinePrimaryPoint}
                          />
                        ) : (
                          <polyline
                            points={totalAssetLinePoints}
                            className={styles.timelinePrimaryLine}
                          />
                        )
                      )}
                      {netAssetLinePoints !== '' && (
                        netAssetSeries.length === 1 ? (
                          <circle
                            cx={chartWidth / 2}
                            cy={chartHeight / 2}
                            r="5"
                            className={styles.timelineSecondaryPoint}
                          />
                        ) : (
                          <polyline
                            points={netAssetLinePoints}
                            className={styles.timelineSecondaryLine}
                          />
                        )
                      )}
                    </svg>
                  </div>
                  <div className={styles.timelineBottomLabels}>
                    {timeline.slice(0, 6).map((item) => (
                      <span key={`${item.turnNumber}-${item.date.toISOString()}`}>
                        {item.turnNumber}턴
                      </span>
                    ))}
                  </div>
                  <div className={styles.timelineList}>
                    {timeline.slice(0, 4).map((item) => (
                      <article key={`${item.turnNumber}-${item.date.toISOString()}`} className={styles.timelineItem}>
                        <div className={styles.timelineMeta}>
                          <span>{item.turnNumber}턴</span>
                          <span>{formatIsoDate(item.date)}</span>
                        </div>
                        <p className={styles.timelineText}>
                          현금 {formatNumber(item.cash)} · 순자산 {formatNumber(item.netAssets)} · 총자산 {formatNumber(item.totalAssets)}
                        </p>
                      </article>
                    ))}
                  </div>
                </>
              ) : (
                <div className={styles.timelineEmpty}>자산 타임라인 기록이 없습니다.</div>
              )}
            </div>

            <div className={styles.reportCard}>
              <div className={styles.reportSectionHeader}>
                <h3 className={styles.reportSectionTitle}>지출 성향</h3>
              </div>
              <div className={styles.spendingInfoColumn}>
                <div className={styles.spendingTop}>
                  <span>{report.spendingPattern?.topCategory ?? '기록 없음'}</span>
                  <span>{formatPercent(report.spendingPattern?.topCategoryRatio ?? null)}</span>
                </div>
                <div className={styles.spendingTrack}>
                  <div
                    className={styles.spendingFill}
                    style={{
                      width: report.spendingPattern?.topCategoryRatio == null
                        ? '0%'
                        : `${Math.round(topCategoryRatio * 100)}%`,
                    }}
                  />
                </div>
                <div className={styles.spendingBreakdown}>
                  <div className={styles.spendingBreakdownRow}>
                    <span>최대 지출 항목</span>
                    <strong>{report.spendingPattern?.topCategory ?? '기록 없음'}</strong>
                  </div>
                </div>
                <p className={styles.spendingText}>이번 회차에서 가장 큰 비중을 차지한 소비 항목입니다.</p>
              </div>
            </div>
          </div>
        </section>

        <section className={styles.reportSection} data-testid="ending-histories">
          <div className={styles.reportSectionHeader}>
            <h2 className={styles.reportSectionTitle}>생활 회고</h2>
          </div>
          <div className={styles.historyBoard}>
            <div className={styles.historyBoardSummary}>
              <span>뉴스 {report.newsHistories.length}건</span>
              <span>이벤트 {report.eventHistories.length}건</span>
              <span>주거 이동 {report.housingHistories.length}건</span>
            </div>

            <div className={styles.historyColumns}>
              <div className={styles.historyColumn}>
                <article className={styles.historyCard} data-testid="ending-news-history">
                  <div className={styles.historyMeta}>
                    <span>뉴스</span>
                    <span>{report.newsHistories.length}건</span>
                  </div>
                  <h3 className={styles.historyTitle}>뉴스 이력</h3>
                  {report.newsHistories.length > 0 ? (
                    <div className={`${styles.historyList} ${styles.historyListScrollable}`}>
                      {report.newsHistories.map((item) => (
                        <div key={`${item.newsId}-${item.turnNumber}`} className={styles.historyListItem}>
                          <div className={styles.historyListMeta}>
                            <span>{item.turnNumber}턴</span>
                            <span>{formatIsoDate(item.publishedDate)}</span>
                          </div>
                          <p className={styles.historyText}>{item.headline}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className={styles.historyEmpty}>기록된 뉴스가 없습니다.</p>
                  )}
                </article>

                <article className={styles.historyCard} data-testid="ending-event-history">
                  <div className={styles.historyMeta}>
                    <span>이벤트</span>
                    <span>{report.eventHistories.length}건</span>
                  </div>
                  <h3 className={styles.historyTitle}>이벤트 이력</h3>
                  {report.eventHistories.length > 0 ? (
                    <div className={`${styles.historyList} ${styles.historyListScrollable}`}>
                      {report.eventHistories.map((item) => (
                        <div key={`${item.gameEventId}-${item.turnNumber}`} className={styles.historyListItem}>
                          <div className={styles.historyListMeta}>
                            <span>{item.turnNumber}턴</span>
                            <span>{item.selectedChoiceCode == null ? '선택 없음' : `선택 ${item.selectedChoiceCode}`}</span>
                          </div>
                          <p className={styles.historyText}>{item.resultSummary ?? '이벤트 결과 기록이 없습니다.'}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className={styles.historyEmpty}>기록된 이벤트가 없습니다.</p>
                  )}
                </article>
              </div>

              <div className={styles.historyColumn}>
                <article className={styles.historyCard} data-testid="ending-housing-history">
                  <div className={styles.historyMeta}>
                    <span>주거 이동</span>
                    <span>{report.housingHistories.length}건</span>
                  </div>
                  <h3 className={styles.historyTitle}>주거 이동 이력</h3>
                  <div className={styles.snapshotRows}>
                    <div className={styles.snapshotRow}>
                      <span>현재 주거</span>
                      <strong>{currentHousingLabel}</strong>
                    </div>
                    <div className={styles.snapshotRow}>
                      <span>목표 주거 달성</span>
                      <strong>
                        {report.housingSnapshot == null
                          ? '-'
                          : hasReachedTargetProperty
                            ? '달성'
                            : '미달성'}
                      </strong>
                    </div>
                  </div>
                  {report.housingHistories.length > 0 ? (
                    <div className={`${styles.historyList} ${styles.historyListScrollable}`}>
                      {report.housingHistories.map((item) => (
                        <div key={`${item.turnNumber}-${item.summary}`} className={styles.historyListItem}>
                          <div className={styles.historyListMeta}>
                            <span>{item.turnNumber}턴</span>
                            <span>{item.summary}</span>
                          </div>
                          <p className={styles.historyText}>
                            {formatHousingState(item.beforeState)} → {formatHousingState(item.afterState)}
                          </p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className={styles.historyEmpty}>주거 이동 기록이 없습니다.</p>
                  )}
                </article>
              </div>
            </div>
          </div>
        </section>
      </section>
    </main>
  );
}
