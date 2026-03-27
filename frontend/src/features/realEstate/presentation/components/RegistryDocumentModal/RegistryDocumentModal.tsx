import { useState, useRef, useCallback, useEffect } from 'react';
import type { RegistryDocument, RegistryRow, SectionSolution } from '../../../domain/entities/PropertyDocument';
import styles from './RegistryDocumentModal.module.css';

type Section = 'gapgu' | 'eulgu';

interface Mark {
  section: Section;
  x: number;
  y: number;
}

interface Props {
  isOpen: boolean;
  onClose: () => void;
  doc: RegistryDocument;
  onSuccess: () => void;
}

const MAGNIFIER_RADIUS = 210;
const MAGNIFIER_ZOOM = 1.7;

export function RegistryDocumentModal({ isOpen, onClose, doc, onSuccess }: Props) {
  const [userMarks, setUserMarks] = useState<Mark[]>([]);
  const [submitted, setSubmitted] = useState(false);
  const [officialMarks, setOfficialMarks] = useState<Mark[]>([]);
  const [magnifierVisible, setMagnifierVisible] = useState(false);
  const [magnifierPos, setMagnifierPos] = useState({ x: 0, y: 0 });
  const [cursorInSource, setCursorInSource] = useState({ x: 0, y: 0 });

  const stageRef = useRef<HTMLDivElement>(null);
  const paperSourceRef = useRef<HTMLDivElement>(null);
  const magnifierContentRef = useRef<HTMLDivElement>(null);
  const gapguCanvasRef = useRef<HTMLDivElement>(null);
  const eulguCanvasRef = useRef<HTMLDivElement>(null);

  const truthSections = (['gapgu', 'eulgu'] as const).filter(
    (s) => doc.solution[s].verdict === '위험',
  );

  const selectedSections = [...new Set(userMarks.map((m) => m.section))];
  const isCorrect =
    selectedSections.length === truthSections.length &&
    truthSections.every((s) => selectedSections.includes(s));

  const syncMagnifier = useCallback(() => {
    if (!paperSourceRef.current || !magnifierContentRef.current) return;
    const clone = paperSourceRef.current.cloneNode(true) as HTMLElement;
    clone.querySelectorAll('[id]').forEach((el) => el.removeAttribute('id'));
    magnifierContentRef.current.replaceChildren(clone);
  }, []);

  useEffect(() => {
    if (isOpen) syncMagnifier();
  }, [isOpen, userMarks, submitted, syncMagnifier]);

  const handleMouseMove = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    if (!stageRef.current || !paperSourceRef.current) return;
    const stageRect = stageRef.current.getBoundingClientRect();
    const sourceRect = paperSourceRef.current.getBoundingClientRect();
    setMagnifierVisible(true);
    setMagnifierPos({ x: e.clientX - stageRect.left, y: e.clientY - stageRect.top });
    setCursorInSource({ x: e.clientX - sourceRect.left, y: e.clientY - sourceRect.top });
  }, []);

  const handleMouseLeave = useCallback(() => setMagnifierVisible(false), []);

  const handleZoneClick = useCallback(
    (section: Section, e: React.MouseEvent<HTMLDivElement>) => {
      if (submitted) return;
      const rect = e.currentTarget.getBoundingClientRect();
      setUserMarks((prev) => [
        ...prev,
        { section, x: e.clientX - rect.left, y: e.clientY - rect.top },
      ]);
    },
    [submitted],
  );

  const buildOfficialMarks = useCallback((): Mark[] => {
    return truthSections
      .map((section) => {
        const canvasEl = section === 'gapgu' ? gapguCanvasRef.current : eulguCanvasRef.current;
        if (!canvasEl) return null;
        const rows = canvasEl.querySelectorAll('tbody tr');
        if (rows.length === 0) return null;
        const lastRow = rows[rows.length - 1] as HTMLElement;
        const canvasRect = canvasEl.getBoundingClientRect();
        const rowRect = lastRow.getBoundingClientRect();
        return {
          section,
          x: Math.min(
            canvasEl.scrollWidth - 42,
            rowRect.left - canvasRect.left + rowRect.width - 36,
          ),
          y: rowRect.top - canvasRect.top + rowRect.height / 2 + 10,
        };
      })
      .filter(Boolean) as Mark[];
  }, [truthSections]);

  const handleSubmit = useCallback(() => {
    if (submitted) {
      if (isCorrect) onSuccess();
      return;
    }
    if (userMarks.length === 0) return;
    setOfficialMarks(buildOfficialMarks());
    setSubmitted(true);
  }, [submitted, userMarks.length, isCorrect, onSuccess, buildOfficialMarks]);

  const handleReset = useCallback(() => {
    setUserMarks([]);
    setSubmitted(false);
    setOfficialMarks([]);
  }, []);

  if (!isOpen) return null;

  const getSectionMarks = (s: Section) => userMarks.filter((m) => m.section === s);
  const formatLabel = (s: Section) => (s === 'gapgu' ? '갑구' : '을구');
  const formatList = (arr: Section[]) =>
    arr.length === 0 ? '없음' : arr.map(formatLabel).join(', ');

  const magnifierTranslateX = MAGNIFIER_RADIUS - cursorInSource.x * MAGNIFIER_ZOOM;
  const magnifierTranslateY = MAGNIFIER_RADIUS - cursorInSource.y * MAGNIFIER_ZOOM;

  return (
    <div className={styles.overlay}>
      <div className={styles.page}>
        <div
          ref={stageRef}
          className={styles.paperStage}
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
        >
          {/* 돋보기 */}
          <div
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
            {submitted && (
              <div className={`${styles.resultBanner} ${isCorrect ? styles.resultSuccess : styles.resultFail}`}>
                <div>
                  <p className={styles.resultEyebrow}>{isCorrect ? '심사 성공' : '심사 실패'}</p>
                  <h2 className={styles.resultTitle}>
                    {isCorrect
                      ? '위험 항목을 정확히 식별했습니다.'
                      : '체크한 구역이 정답과 일치하지 않습니다.'}
                  </h2>
                  <p className={styles.resultCopy}>
                    {isCorrect
                      ? '공식 체크가 실제 문제 위치에 표시됩니다. 다음 단계로 진행하세요.'
                      : '공식 체크가 실제 문제 위치에 표시됩니다. 해설을 확인하세요.'}
                  </p>
                </div>
                <div className={styles.resultGrid}>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>내가 체크한 구역</span>
                    <strong className={styles.resultBoxValue}>{formatList(selectedSections as Section[])}</strong>
                  </div>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>정답 구역</span>
                    <strong className={styles.resultBoxValue}>{formatList(truthSections as Section[])}</strong>
                  </div>
                  <div className={styles.resultBox}>
                    <span className={styles.resultBoxLabel}>다음 흐름</span>
                    <strong className={styles.resultBoxValue}>{isCorrect ? '다음 단계로' : '게임 오버'}</strong>
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
                  문서를 읽고 위험한 구역을 클릭해 표시한 다음 제출하세요.
                </p>
              </header>

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
                section="gapgu"
                rows={doc.gapguRows as RegistryRow[]}
                userMarks={submitted ? [] : getSectionMarks('gapgu')}
                answerMarks={submitted ? officialMarks.filter((m) => m.section === 'gapgu') : []}
                explanation={
                  submitted
                    ? {
                        myChecked: getSectionMarks('gapgu').length,
                        isDangerous: truthSections.includes('gapgu'),
                        solution: doc.solution.gapgu,
                      }
                    : null
                }
                onCanvasClick={(e) => handleZoneClick('gapgu', e)}
                canvasRef={gapguCanvasRef}
              />

              {/* 을구 */}
              <RegistrySection
                index="Section 3"
                title="을구"
                subtitle="소유권 이외 권리에 관한 사항"
                section="eulgu"
                rows={doc.eulguRows as RegistryRow[]}
                userMarks={submitted ? [] : getSectionMarks('eulgu')}
                answerMarks={submitted ? officialMarks.filter((m) => m.section === 'eulgu') : []}
                explanation={
                  submitted
                    ? {
                        myChecked: getSectionMarks('eulgu').length,
                        isDangerous: truthSections.includes('eulgu'),
                        solution: doc.solution.eulgu,
                      }
                    : null
                }
                onCanvasClick={(e) => handleZoneClick('eulgu', e)}
                canvasRef={eulguCanvasRef}
              />
            </main>
          </div>
        </div>

        {/* 푸터 */}
        <section className={styles.footerBar}>
          <div>
            <p className={styles.footerEyebrow}>
              {submitted ? (isCorrect ? '정답 처리' : '오답 처리') : '심사 전'}
            </p>
            <p className={styles.footerText}>
              {!submitted
                ? userMarks.length === 0
                  ? '돋보기로 문서를 살펴본 다음, 문제가 있다고 느껴지는 구역을 직접 클릭하세요.'
                  : `현재 체크된 구역: ${formatList(selectedSections as Section[])}. 제출하거나 다른 구역도 체크하세요.`
                : isCorrect
                  ? '위험 항목을 모두 찾았습니다. 다음 단계로 진행할 수 있습니다.'
                  : '일부 위험 항목을 놓쳤습니다. 해설을 확인하세요.'}
            </p>
          </div>
          <div className={styles.footerActions}>
            <button className={styles.actionButton} onClick={handleReset} type="button">
              다시 확인하기
            </button>
            <button
              className={`${styles.actionButton} ${styles.actionButtonPrimary}`}
              onClick={handleSubmit}
              disabled={!submitted && userMarks.length === 0}
              type="button"
            >
              {submitted ? (isCorrect ? '다음 단계로' : '게임 오버') : '체크 제출'}
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}

interface SectionProps {
  index: string;
  title: string;
  subtitle: string;
  section: Section;
  rows: RegistryRow[];
  userMarks: Mark[];
  answerMarks: Mark[];
  explanation: {
    myChecked: number;
    isDangerous: boolean;
    solution: SectionSolution;
  } | null;
  onCanvasClick: (e: React.MouseEvent<HTMLDivElement>) => void;
  canvasRef: React.RefObject<HTMLDivElement | null>;
}

function RegistrySection({
  index, title, subtitle, rows,
  userMarks, answerMarks, explanation,
  onCanvasClick, canvasRef,
}: SectionProps) {
  const markCount = userMarks.length + answerMarks.length;

  return (
    <section className={styles.section}>
      <div className={styles.sectionHeader}>
        <p className={styles.sectionIndex}>{index}</p>
        <h3 className={styles.sectionTitle}>{title}</h3>
        <p className={styles.sectionSubtitle}>{subtitle}</p>
      </div>

      <div className={styles.interactiveZone}>
        <div className={styles.zoneHead}>
          <span className={styles.zoneLabel}>문제라고 의심되는 위치를 클릭하세요</span>
          <span className={styles.zoneHint}>{title} 영역이라면 줄 사이 여백을 눌러도 체크가 남습니다.</span>
        </div>
        <div className={styles.zoneScroll}>
          <div
            ref={canvasRef}
            className={styles.zoneCanvas}
            onClick={onCanvasClick}
          >
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

            <div className={styles.markLayer}>
              {userMarks.map((m, i) => (
                <span
                  key={`u-${i}`}
                  className={`${styles.mark} ${styles.markUser}`}
                  style={{ left: m.x, top: m.y }}
                >✓</span>
              ))}
            </div>
            <div className={styles.markLayer}>
              {answerMarks.map((m, i) => (
                <span
                  key={`a-${i}`}
                  className={`${styles.mark} ${styles.markAnswer}`}
                  style={{ left: m.x, top: m.y }}
                >✓</span>
              ))}
            </div>
          </div>
        </div>
        <div className={styles.markCount}>
          {markCount === 0 ? '체크 없음' : `${title} 영역 체크 ${markCount}개`}
        </div>
      </div>

      {explanation && (
        <div className={styles.explanationCard}>
          <div className={styles.explanationTop}>
            <div className={styles.badgeWrap}>
              <span className={styles.badgeLabel}>내 체크</span>
              <span className={styles.badge}>
                {explanation.myChecked > 0 ? `체크 ${explanation.myChecked}개` : '미체크'}
              </span>
            </div>
            <div className={styles.badgeWrap}>
              <span className={styles.badgeLabel}>정답 상태</span>
              <span className={styles.badge}>{explanation.isDangerous ? '문제 있음' : '정상'}</span>
            </div>
          </div>
          <p className={styles.issueSummary}>{explanation.solution.issueSummary}</p>
          <div className={styles.explanationGrid}>
            <div>
              <p className={styles.blockTitle}>핵심 포인트</p>
              <ul className={styles.points}>
                {explanation.solution.keyPoints.map((pt, i) => (
                  <li key={i}>{pt}</li>
                ))}
              </ul>
            </div>
            <div className={styles.feedbackBox}>
              <p className={styles.blockTitle}>
                {(explanation.myChecked > 0) === explanation.isDangerous ? '정답 피드백' : '오답 피드백'}
              </p>
              <p>
                {(explanation.myChecked > 0) === explanation.isDangerous
                  ? explanation.solution.feedbackCorrect
                  : explanation.solution.feedbackWrong}
              </p>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
