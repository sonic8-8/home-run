import clsx from 'clsx';
import { AuthImage } from '@shared/components/AuthImage/AuthImage';
import { formatMoney } from '@shared/utils/formatter';
import type { CardProduct } from '@features/card/domain/entities/Card';
import styles from './CardDetail.module.css';

type CardDetailLayout = 'panel' | 'page';

interface CardDetailProps {
  card: CardProduct;
  layout?: CardDetailLayout;
  onBack: () => void;
}

function formatAmount(amount: number | null, fallback: string): string {
  if (amount === null || amount <= 0) {
    return fallback;
  }

  return `${formatMoney(amount)}원`;
}

function formatDiscountRate(discountRate: number): string {
  return `${discountRate}% 할인`;
}

export function CardDetail({
  card,
  layout = 'panel',
  onBack,
}: CardDetailProps) {
  return (
    <div
      className={clsx(
        styles.root,
        layout === 'page' ? styles.pageLayout : styles.panelLayout,
      )}
      data-testid="card-detail"
    >
      <div className={styles.hero}>
        <button
          type="button"
          className={styles.backButton}
          onClick={onBack}
        >
          ← 목록으로
        </button>

        <div className={styles.heroBody}>
          <div className={styles.visualFrame}>
            <div className={styles.visualFallback}>
              <span className={styles.visualFallbackText}>{card.issuerName}</span>
            </div>
            <AuthImage
              src={card.imageUrl}
              alt={card.name}
              className={styles.visualImage}
            />
          </div>

          <div className={styles.heroInfo}>
            <span className={styles.issuer}>{card.issuerName}</span>
            <h2 className={styles.title}>{card.name}</h2>
            <p className={styles.description}>{card.description}</p>
            <div className={styles.metrics}>
              <div className={styles.metricCard}>
                <span className={styles.metricLabel}>전월 실적 기준</span>
                <strong className={styles.metricValue}>
                  {formatAmount(card.baselinePerformanceAmount, '조건 없음')}
                </strong>
              </div>
              <div className={styles.metricCard}>
                <span className={styles.metricLabel}>최대 혜택 한도</span>
                <strong className={styles.metricValue}>
                  {formatAmount(card.maxBenefitLimitAmount, '제한 정보 없음')}
                </strong>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.content}>
        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>혜택 요약</h3>
          {card.activeBenefits.length === 0 ? (
            <p className={styles.emptyText}>등록된 혜택 정보가 없습니다.</p>
          ) : (
            <ul className={styles.benefitList}>
              {card.activeBenefits.map((benefit) => (
                <li key={`${card.id}-${benefit.categoryId}-${benefit.categoryName}`} className={styles.benefitItem}>
                  <div className={styles.benefitHeader}>
                    <strong className={styles.benefitName}>{benefit.categoryName}</strong>
                    <span className={styles.benefitRate}>{formatDiscountRate(benefit.discountRate)}</span>
                  </div>
                  <p className={styles.benefitDescription}>{benefit.categoryDescription}</p>
                  {benefit.exampleMerchants.length > 0 && (
                    <div className={styles.merchantChips}>
                      {benefit.exampleMerchants.map((merchant) => (
                        <span key={`${benefit.categoryId}-${merchant}`} className={styles.merchantChip}>
                          {merchant}
                        </span>
                      ))}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>지원 범위 안내</h3>
          <div className={styles.noticeBox}>
            <p className={styles.noticeText}>
              현재 프론트엔드는 카드 목록과 추천 조회만 연동되어 있습니다.
            </p>
            <p className={styles.noticeText}>
              카드 신청, 보유 카드 조회, 카드 해지 API는 백엔드 계약이 없어 준비 중 상태로 표시합니다.
            </p>
          </div>
        </section>

        <button
          type="button"
          className={styles.applyButton}
          disabled
          aria-disabled="true"
        >
          카드 신청 API 준비 중
        </button>
      </div>
    </div>
  );
}
