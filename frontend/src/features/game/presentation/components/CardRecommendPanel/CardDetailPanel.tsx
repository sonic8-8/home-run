import styles from './CardDetailPanel.module.css';

export interface CardProduct {
  cardId: number;
  cardName: string;
  cardImageFrontUrl: string;
  category: 'travel' | 'gas' | 'life' | 'recommended';
  bgColor: string;
  icon: string;
  annualFee: number;
  annualBenefit: number;
  summary: string;
  benefits: string[];
  rewardConditions: { condition: string; detail: string }[];
}

interface Props {
  card: CardProduct;
  onBack: () => void;
}

export function CardDetailPanel({ card, onBack }: Props) {
  return (
    <div className={styles.panel}>
      {/* ── 상단 Hero 영역 ── */}
      <div
        className={styles.hero}
        style={{ background: `linear-gradient(135deg, ${card.bgColor}cc, ${card.bgColor}55)` }}
      >
        <button className={styles.backBtn} onClick={onBack}>← 뒤로</button>

        <div className={styles.heroBody}>
          {/* 카드 이미지 */}
          <div className={styles.cardWrap}>
            {card.cardImageFrontUrl ? (
              <img src={card.cardImageFrontUrl} alt={card.cardName} className={styles.cardImg} />
            ) : (
              <div className={styles.cardPlaceholder} style={{ backgroundColor: card.bgColor }}>
                <span className={styles.cardIcon}>{card.icon}</span>
              </div>
            )}
          </div>

          {/* 카드명 + 연간 혜택 */}
          <div className={styles.heroInfo}>
            <h2 className={styles.cardName}>{card.cardName}</h2>
            <div className={styles.benefitBadge}>
              연간 최대&nbsp;<strong>{card.annualBenefit.toLocaleString('ko-KR')}원</strong>&nbsp;혜택
            </div>
            <p className={styles.summary}>{card.summary}</p>
          </div>
        </div>
      </div>

      {/* ── 하단 상세 정보 ── */}
      <div className={styles.detail}>

        {/* 서비스 안내 */}
        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>서비스 안내</h3>
          <ul className={styles.benefitList}>
            {card.benefits.map((b, i) => (
              <li key={i} className={styles.benefitItem}>
                <span className={styles.checkIcon}>✓</span>
                {b}
              </li>
            ))}
          </ul>
        </section>

        {/* 이용 조건 */}
        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>이용 조건</h3>
          <div className={styles.conditionList}>
            {card.rewardConditions.map((rc, i) => (
              <div key={i} className={styles.conditionItem}>
                <span className={styles.conditionLabel}>{rc.condition}</span>
                <span className={styles.conditionDetail}>{rc.detail}</span>
              </div>
            ))}
          </div>
        </section>

        {/* 연회비 */}
        <div className={styles.feeRow}>
          <span className={styles.feeLabel}>카드 연회비</span>
          <span className={styles.feeValue}>
            {card.annualFee === 0 ? '면제' : `${card.annualFee.toLocaleString('ko-KR')}원`}
          </span>
        </div>
      </div>
    </div>
  );
}

export default CardDetailPanel;
