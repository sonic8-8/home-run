import { useGameMain } from '@features/game/presentation/hooks/useGameMain';
import { AssetsDetailModal } from '@features/game/presentation/components/AssetsDetailModal/AssetsDetailModal';
import { LoanProductsPanel } from '@features/game/presentation/components/LoanProductsPanel/LoanProductsPanel';
import { CardRecommendPanel } from '@features/game/presentation/components/CardRecommendPanel/CardRecommendPanel';
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
  const {
    assets, stats, currentDate, characterType, totalAssets,
    isModalOpen, openModal, closeModal,
    leftView, setLeftView,
  } = useGameMain();

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
              {leftView === 'loan'     && <LoanProductsPanel />}
              {leftView === 'property' && <div className={styles.placeholder}>부동산 — 준비 중</div>}
              {leftView === 'card'     && <CardRecommendPanel />}
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
                  className={leftView === 'property' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'property' ? 'scene' : 'property')}
                >
                  부동산 알아보기
                </button>
                <button
                  className={leftView === 'card' ? styles.menuButtonActive : styles.menuButton}
                  onClick={() => setLeftView(leftView === 'card' ? 'scene' : 'card')}
                >
                  카드 추천
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
    </>
  );
}

export default GameMain;
