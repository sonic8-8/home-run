import { useRef, useCallback, useEffect, useState, type ReactNode } from 'react';
import type {
  ContractResponse,
  RegistryDocument,
  RegistryRow,
  SectionSolution,
} from '../../../domain/entities/PropertyDocument';
import styles from './RegistryDocumentModal.module.css';

interface RegistryDocumentModalProps {
  isOpen: boolean;
  onClose: () => void;
  doc: RegistryDocument;
  selectedTrapIds: readonly string[];
  isSubmitting: boolean;
  submitError: string | null;
  reviewResult: ContractResponse | null;
  onToggleTrap: (trapId: string) => void;
  onResetSelection: () => void;
  onSubmitReview: () => void;
}

const MAGNIFIER_RADIUS = 210;
const MAGNIFIER_ZOOM = 1.7;

function formatContractResult(contractResult: ContractResponse['contractResult']): string {
  if (contractResult === 'SAFE') {
    return '안전';
  }
  if (contractResult === 'PARTIAL') {
    return '주의';
  }
  return '위험';
}

function getResultTitle(reviewResult: ContractResponse): string {
  if (reviewResult.success) {
    return '계약 검토를 제출했습니다.';
  }

  return '계약 검토 결과를 확인해주세요.';
}

function getResultEyebrow(reviewResult: ContractResponse): string {
  if (reviewResult.success) {
    return '심사 통과';
  }

  return '진행 불가';
}

function getFooterCopy(
  selectedTrapIds: readonly string[],
  isSubmitting: boolean,
  reviewResult: ContractResponse | null,
): string {
  if (reviewResult !== null) {
    return '결과를 확인한 뒤 모달을 닫고 부동산 화면으로 돌아갑니다. 이번 범위에서는 구매 확정까지 이어지지 않습니다.';
  }

  if (isSubmitting) {
    return '선택한 체크리스트를 제출하고 있습니다. 잠시만 기다려주세요.';
  }

  if (selectedTrapIds.length === 0) {
    return '문서를 검토한 뒤 아래 체크리스트에서 위험하다고 판단한 항목을 선택하세요.';
  }

  return `현재 ${selectedTrapIds.length}개 항목을 선택했습니다. 제출하면 백엔드 계약 검토 API로 결과를 확인합니다.`;
}

export function RegistryDocumentModal({
  isOpen,
  onClose,
  doc,
  selectedTrapIds,
  isSubmitting,
  submitError,
  reviewResult,
  onToggleTrap,
  onResetSelection,
  onSubmitReview,
}: RegistryDocumentModalProps) {
  const [magnifierVisible, setMagnifierVisible] = useState(false);
  const [magnifierPos, setMagnifierPos] = useState({ x: 0, y: 0 });
  const [cursorInSource, setCursorInSource] = useState({ x: 0, y: 0 });

  const stageRef = useRef<HTMLDivElement>(null);
  const paperSourceRef = useRef<HTMLDivElement>(null);
  const magnifierContentRef = useRef<HTMLDivElement>(null);

  const syncMagnifier = useCallback(() => {
    if (!paperSourceRef.current || !magnifierContentRef.current) return;
    const clone = paperSourceRef.current.cloneNode(true) as HTMLElement;
    clone.querySelectorAll('[id]').forEach((el) => el.removeAttribute('id'));
    magnifierContentRef.current.replaceChildren(clone);
  }, []);

  useEffect(() => {
    if (isOpen) syncMagnifier();
  }, [isOpen, syncMagnifier]);

  const handleMouseMove = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    if (!stageRef.current || !paperSourceRef.current) return;
    const stageRect = stageRef.current.getBoundingClientRect();
    const sourceRect = paperSourceRef.current.getBoundingClientRect();
    setMagnifierVisible(true);
    setMagnifierPos({ x: e.clientX - stageRect.left, y: e.clientY - stageRect.top });
    setCursorInSource({ x: e.clientX - sourceRect.left, y: e.clientY - sourceRect.top });
  }, []);

  const handleMouseLeave = useCallback(() => setMagnifierVisible(false), []);

  if (!isOpen) return null;

  const magnifierTranslateX = MAGNIFIER_RADIUS - cursorInSource.x * MAGNIFIER_ZOOM;
  const magnifierTranslateY = MAGNIFIER_RADIUS - cursorInSource.y * MAGNIFIER_ZOOM;
  const submitted = reviewResult !== null;
  const submitDisabled = !submitted && (selectedTrapIds.length === 0 || isSubmitting);

  return (
    <div className={styles.overlay}>
      <div className={styles.page} data-guide="property-registry-modal">
        <div
          ref={stageRef}
          className={styles.paperStage}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
        >
          {/* 돋보기 */}
          <div
            aria-hidden="true"
            className={`${styles.magnifier} ${magnifierVisible ? styles.magnifierVisible : ''}`}
            style={{ left: magnifierPos.x - MAGNIFIER_RADIUS, top: magnifierPos.y - MAGNIFIER_RADIUS }}
          >
            <div
              ref={magnifierContentRef}
              className={styles.magnifierContent}
              style={{
                transform: `translate(${magnifierTranslateX}px, ${magnifierTranslateY}px) scale(${MAGNIFIER_ZOOM})`,
              }}
            />
          </div>

          <div ref={paperSourceRef}>
            {/* 결과 배너 */}
            {reviewResult && (
              <div
                className={`${styles.resultBanner} ${reviewResult.success ? styles.resultSuccess : styles.resultFail}`}
              >
                <div>
                  <p className={styles.resultEyebrow}>{getResultEyebrow(reviewResult)}</p>
                  <h2 className={styles.resultTitle}>{getResultTitle(reviewResult)}</h2>
                  <p className={styles.resultCopy}>{reviewResult.message}</p>
                </div>
                <div className={styles.resultGrid}>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>선택 항목</span>
                    <strong className={styles.resultBoxValue}>{selectedTrapIds.length}개</strong>
                  </div>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>식별한 함정</span>
                    <strong className={styles.resultBoxValue}>
                      {reviewResult.trapsCorrectlyIdentified}/{reviewResult.trapsDetected}
                    </strong>
                  </div>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>계약 판정</span>
                    <strong className={styles.resultBoxValue}>
                      {formatContractResult(reviewResult.contractResult)}
                    </strong>
                  </div>
                </div>
              </div>
            )}

            {/* 본문 페이퍼 */}
            <main className={styles.paper}>
              <button className={styles.closeButton} onClick={onClose} type="button">✕</button>

              <header className={styles.paperHead}>
                <p className={styles.paperSubtitle}>등기부등본 미니게임</p>
                <h2 className={styles.paperTitle}>등기사항전부증명서</h2>
                <p className={styles.paperCaption}>
                  문서를 읽고 체크리스트에서 위험하다고 판단한 항목을 선택한 뒤 제출하세요.
                </p>
              </header>

              <section className={styles.section}>
                <div className={styles.sectionHeader}>
                  <p className={styles.sectionIndex}>Checklist</p>
                  <h3 className={styles.sectionTitle}>계약 검토 체크리스트</h3>
                  <p className={styles.sectionSubtitle}>
                    문서에서 확인한 위험 요소를 체크하세요. 선택한 항목 ID 배열이 review API의
                    <code> checkedTraps </code>
                    로 제출됩니다.
                  </p>
                </div>
                <div className={styles.checklistCard} data-guide="property-registry-checklist">
                  <div className={styles.checklistMeta}>
                    <span className={styles.badge}>선택 {selectedTrapIds.length}개</span>
                    <span className={styles.badge}>전체 {doc.checklistItems.length}개</span>
                  </div>
                  <div className={styles.checklistList}>
                    {doc.checklistItems.map((item) => {
                      const checked = selectedTrapIds.includes(item.trapId);
                      return (
                        <label
                          key={item.trapId}
                          className={`${styles.checklistItem} ${checked ? styles.checklistItemSelected : ''}`}
                        >
                          <input
                            className={styles.checklistInput}
                            type="checkbox"
                            checked={checked}
                            disabled={submitted || isSubmitting}
                            onChange={() => onToggleTrap(item.trapId)}
                          />
                          <span className={styles.checklistLabel}>{item.label}</span>
                        </label>
                      );
                    })}
                  </div>
                  {submitError && <p className={styles.inlineError}>{submitError}</p>}
                </div>
              </section>

              {/* 표제부 */}
              <section className={styles.section}>
                <div className={styles.sectionHeader}>
                  <p className={styles.sectionIndex}>Section 1</p>
                  <h3 className={styles.sectionTitle}>표제부</h3>
                  <p className={styles.sectionSubtitle}>표제부는 참고 정보이며 클릭 대상이 아닙니다.</p>
                </div>
                <table className={styles.registryInfo}>
                  <tbody>
                    <tr>
                      <th scope="row">부동산명</th>
                      <td>{doc.propertyName}</td>
                    </tr>
                    <tr>
                      <th scope="row">소재지번</th>
                      <td>{doc.address}</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              {/* 갑구 */}
              <RegistrySection
                index="Section 2"
                title="갑구"
                subtitle="소유권에 관한 사항"
                rows={doc.gapguRows}
                solution={doc.solution.gapgu}
                submitted={submitted}
              />

              {/* 을구 */}
              <RegistrySection
                index="Section 3"
                title="을구"
                subtitle="소유권 이외 권리에 관한 사항"
                rows={doc.eulguRows}
                solution={doc.solution.eulgu}
                submitted={submitted}
              />
            </main>
          </div>
        </div>

        {/* 푸터 */}
        <section className={styles.footerBar}>
          <div>
            <p className={styles.footerEyebrow}>
              {submitted ? '심사 결과' : '체크리스트 선택'}
            </p>
            <p className={styles.footerText}>
              {getFooterCopy(selectedTrapIds, isSubmitting, reviewResult)}
            </p>
          </div>
          <div className={styles.footerActions}>
            {!submitted && (
              <button className={styles.actionButton} onClick={onResetSelection} type="button">
                선택 초기화
              </button>
            )}
            <button
              className={`${styles.actionButton} ${styles.actionButtonPrimary}`}
              onClick={submitted ? onClose : onSubmitReview}
              disabled={submitDisabled}
              type="button"
            >
              {submitted ? '확인' : isSubmitting ? '제출 중...' : '검토 제출'}
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}

interface RegistrySectionProps {
  index: string;
  title: string;
  subtitle: string;
  rows: readonly RegistryRow[];
  solution: SectionSolution;
  submitted: boolean;
}

function RegistrySection({
  index,
  title,
  subtitle,
  rows,
  solution,
  submitted,
}: RegistrySectionProps) {
  const verdictBadge = solution.verdict === '위험' ? '문제 있음' : '정상';

  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <p className={styles.sectionIndex}>{index}</p>
        <h3 className={styles.sectionTitle}>{title}</h3>
        <p className={styles.sectionSubtitle}>{subtitle}</p>
      </div>

      <div className={styles.interactiveZone}>
        <div className={styles.zoneHead}>
          <span className={styles.zoneLabel}>등기 항목</span>
          <span className={styles.zoneHint}>행별 내용을 확인하며 위 체크리스트를 선택하세요.</span>
        </div>
        <div className={styles.zoneScroll}>
          <div className={`${styles.zoneCanvas} ${styles.zoneCanvasReadonly}`}>
            <table className={styles.registryTable}>
              <thead>
                <tr>
                  <th scope="col">순위번호</th>
                  <th scope="col">등기목적</th>
                  <th scope="col">접수</th>
                  <th scope="col">등기원인</th>
                  <th scope="col">권리자 및 기타사항</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', color: '#9ca3af' }}>기록사항 없음</td>
                  </tr>
                ) : (
                  rows.map((row, i) => (
                    <tr key={i}>
                      <td>{row.rankNo}</td>
                      <td>{row.purpose}</td>
                      <td>{row.receipt}</td>
                      <td>{row.reason}</td>
                      <td>{row.details}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {submitted && (
        <div className={styles.explanationCard}>
          <div className={styles.explanationTop}>
            <div className={styles.badgeWrap}>
              <span className={styles.badgeLabel}>정답 상태</span>
              <span className={styles.badge}>{verdictBadge}</span>
            </div>
          </div>
          <p className={styles.issueSummary}>{solution.issueSummary}</p>
          <div className={styles.explanationGrid}>
            <div>
              <p className={styles.blockTitle}>핵심 포인트</p>
              <ul className={styles.points}>
                {solution.keyPoints.map((pt, i) => (
                  <li key={i}>{pt}</li>
                ))}
              </ul>
            </div>
            <div className={styles.feedbackBox}>
              <p className={styles.blockTitle}>해설</p>
              <FeedbackLine label="정답 시 안내">{solution.feedbackCorrect}</FeedbackLine>
              <FeedbackLine label="놓쳤을 때 안내">{solution.feedbackWrong}</FeedbackLine>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

interface FeedbackLineProps {
  label: string;
  children: ReactNode;
}

function FeedbackLine({
  label,
  children,
}: FeedbackLineProps) {
  return (
    <>
      <p className={styles.feedbackLabel}>{label}</p>
      <p className={styles.feedbackText}>{children}</p>
    </>
  );
}
