import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { useGameMain } from '@features/game/presentation/hooks/useGameMain';
import { AssetsDetailModal } from '@features/game/presentation/components/AssetsDetailModal/AssetsDetailModal';
import { NewsEventModal } from '@features/game/presentation/components/NewsEventModal/NewsEventModal';
import { LoanProductsPanel } from '@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel';
import { CardRecommendPanel } from '@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel';
import { LoanReviewResultModal } from '@features/loan/presentation/components/LoanReviewResultModal';
import { LoanConfirmModal } from '@features/loan/presentation/components/LoanConfirmModal';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';
import styles from './GameMain.module.css';

// 부동산 보유 유형별 씬 이미지
import sceneRoad from '@assets/images/game_back_road.png';

const SCENE_BY_HOUSING: Record<string, string> = {
  NONE:        sceneRoad,  // 자가 없음 — 거리 씬 (기본)
  STUDIO:      sceneRoad,  // TODO: 원룸 씬
  VILLA:       sceneRoad,  // TODO: 빌라 씬
  JEONSE_APT:  sceneRoad,  // TODO: 전세 아파트 씬
  OWNED_APT:   sceneRoad,  // TODO: 자가 아파트 씬
};

// 캐릭터 유형별 이미지 (public 폴더)
const CHARACTER_IMAGE: Record<string, string> = {
  MALE:   '/assets/images/bcharac.png',
  FEMALE: '/assets/images/gcharac.png',
};

const STAT_ITEMS = [
  { label: '지능', key: 'knowledge' },
  { label: '체력', key: 'health' },
  { label: '피로도', key: 'fatigue' },
  { label: '스트레스', key: 'stress' },
  { label: '행복도', key: 'happiness' },
] as const;

function formatMoney(amount: number | undefined): string {
  if (amount === undefined) return '0';
  return amount.toLocaleString('ko-KR');
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '';
  const [year, month, day] = dateStr.split('-');
  return `${year}년 ${String(Number(month)).padStart(2, '0')}월 ${String(Number(day)).padStart(2, '0')}일`;
}

export function GameMain() {
  const navigate = useNavigate();
  const {
    sessionId,
    assets, stats, currentDate, characterType, totalAssets,
    isModalOpen, openModal, closeModal,
    leftView, setLeftView,
    preSelectedPropertyId, preSelectedPropertyName, preSelectedPropertyPrice,
  } = useGameMain();

  const [newsOpen, setNewsOpen] = useState(false);

  // 새 달(턴)이 시작될 때 뉴스 모달 자동 오픈
  // sessionStorage에 마지막으로 뉴스를 본 날짜를 저장해 중복 방지
  useEffect(() => {
    if (!currentDate || !sessionId) return;
    const storageKey = `news_seen_date_${sessionId}`;
    const lastSeen = sessionStorage.getItem(storageKey);
    if (lastSeen !== currentDate) {
      setNewsOpen(true);
      sessionStorage.setItem(storageKey, currentDate);
    }
  }, [currentDate, sessionId]);

  const [loanApplication, setLoanApplication] = useState<LoanApplication | null>(null);
  const [reviewOpen, setReviewOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const handleApplyDirect = (_propertyId: string, propertyName: string, propertyPrice: number) => {
    setLoanApplication({
      applicationId: 'APP-001',
      status: 'APPROVED',
      requestInfo: {
        propertyName,
        propertyPrice,
        applicationDate: new Date().toISOString().slice(0, 10),
      },
      result: { maxLoanAmount: Math.round(propertyPrice * 0.7) },
    });
    setReviewOpen(true);
  };

  const housingType = assets?.realEstate?.housingType ?? 'NONE';
  const sceneImage = SCENE_BY_HOUSING[housingType] ?? SCENE_BY_HOUSING['NONE'];
  const characterImage = CHARACTER_IMAGE[characterType];

  return (
    <>
      <div className={styles.page}>
        <div className={styles.container}>

          {/* 왼쪽 */}
          {leftView === 'scene' ? (
            <div
              className={styles.scene}
              style={{ backgroundImage: `url(${sceneImage})` }}
            >
              <div className={styles.dateLabel}>
                <span className={styles.calendarIcon}>📅</span>
                {formatDate(currentDate)}
              </div>
              <img src={characterImage} alt="캐릭터" className={styles.character} />
            </div>
          ) : (
            <div className={styles.leftPanel}>
              {leftView === 'loan'  && (
                <LoanProductsPanel
                  sessionId={sessionId}
                  preSelectedPropertyId={preSelectedPropertyId}
                  preSelectedPropertyName={preSelectedPropertyName}
                  preSelectedPropertyPrice={preSelectedPropertyPrice}
                  onApplyDirect={handleApplyDirect}
                />
              )}
              {leftView === 'card'  && <CardRecommendPanel />}
            </div>
          )}

          {/* 오른쪽: 정보 패널 */}
          <div className={styles.panel}>
            {/* 내 자산 */}
            <section className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>내 자산</h2>
                <button className={styles.detailLink} onClick={openModal}>자세히 보기 &gt;</button>
              </div>
              <div className={styles.assetList}>
                <div className={styles.assetRow}>
                  <span className={styles.assetLabel}>현금 자산</span>
                  <span className={styles.assetValue}>{formatMoney(assets?.cash)} 원</span>
                </div>
                <div className={styles.assetRow}>
                  <span className={styles.assetLabel}>대출</span>
                  <span className={styles.assetValue}>{formatMoney(assets?.loan?.principal)} 원</span>
                </div>
                <div className={styles.assetRow}>
                  <span className={styles.assetLabel}>부동산 자산</span>
                  <span className={styles.assetValue}>{formatMoney(assets?.realEstate?.currentValue ?? 0)} 원</span>
                </div>
                <div className={styles.assetRow}>
                  <span className={styles.assetLabelBold}>총 자산</span>
                  <span className={styles.assetValueBold}>{formatMoney(totalAssets ?? 0)} 원</span>
                </div>
              </div>
            </section>

            {/* 메뉴 */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>메뉴</h2>
              <div className={styles.menuList}>
                <button
                  className={leftView === 'loan' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'loan' ? 'scene' : 'loan')}
                >
                  대출 알아보기
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => navigate(ROUTES.PROPERTY, { state: { sessionId, mode: 'browse' } })}
                >
                  부동산 알아보기
                </button>
                <button
                  className={leftView === 'card' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'card' ? 'scene' : 'card')}
                >
                  카드 추천
                </button>
                <button
                  className={styles.menuButton}
                  onClick={() => setNewsOpen(true)}
                >
                  이달의 뉴스
                </button>
              </div>
            </section>

            {/* USER 스탯 */}
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>USER</h2>
              <div className={styles.statList}>
                {STAT_ITEMS.map(({ label, key }) => {
                  const value = stats?.[key] ?? 0;
                  return (
                    <div key={key} className={styles.statRow}>
                      <span className={styles.statLabel}>{label}</span>
                      <div className={styles.statBarTrack}>
                        <div className={styles.statBarFill} style={{ width: `${value}%` }} />
                      </div>
                      <span className={styles.statValue}>{value}</span>
                    </div>
                  );
                })}
              </div>
            </section>
          </div>
        </div>
      </div>

      <AssetsDetailModal isOpen={isModalOpen} onClose={closeModal} assets={assets} />
      <NewsEventModal isOpen={newsOpen} onClose={() => setNewsOpen(false)} sessionId={sessionId} />

      {loanApplication && (
        <>
          <LoanReviewResultModal
            isOpen={reviewOpen}
            onClose={() => { setReviewOpen(false); setLoanApplication(null); }}
            onGoToProperty={() => { setReviewOpen(false); setConfirmOpen(true); }}
            application={loanApplication}
          />
          <LoanConfirmModal
            isOpen={confirmOpen}
            onClose={() => { setConfirmOpen(false); setLoanApplication(null); }}
            onConfirm={() => { setConfirmOpen(false); setLoanApplication(null); }}
            applicationId={loanApplication.applicationId}
            contractorName="플레이어"
            maxLoanAmount={loanApplication.result.maxLoanAmount ?? 0}
          />
        </>
      )}
    </>
  );
}

export default GameMain;
