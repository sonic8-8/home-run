import { useState } from 'react';
import type { CardRecommendation } from '../../../domain/entities/CardRecommendation';
import { AuthImage } from '@shared/components/AuthImage/AuthImage';
import styles from './CardModal.module.css';

type Tab = 'recommend' | 'my';

interface CardModalProps {
  isOpen: boolean;
  onClose: () => void;
  recommendations: CardRecommendation[];
  allCards: CardRecommendation[];
  onApply: (card: CardRecommendation) => void;
}

export function CardModal({ isOpen, onClose, recommendations, allCards, onApply }: CardModalProps) {
  const [activeTab, setActiveTab] = useState<Tab>('recommend');
  const [selectedCard, setSelectedCard] = useState<CardRecommendation | null>(null);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 탭 */}
        <div className={styles.tabs}>
          <button
            className={activeTab === 'recommend' ? `${styles.tab} ${styles.tabActive}` : styles.tab}
            onClick={() => { setActiveTab('recommend'); setSelectedCard(null); }}
          >
            추천 카드
          </button>
          <button
            className={activeTab === 'my' ? `${styles.tab} ${styles.tabActive}` : styles.tab}
            onClick={() => { setActiveTab('my'); setSelectedCard(null); }}
          >
            전체 카드
          </button>
        </div>

        {/* 추천 카드 탭 */}
        {activeTab === 'recommend' && (
          <div className={styles.content}>
            {selectedCard ? (
              /* 카드 상세 */
              <div className={styles.detail}>
                <button className={styles.backButton} onClick={() => setSelectedCard(null)}>
                  ← 목록으로
                </button>
                <div className={styles.detailImageWrap}>
                  <AuthImage
                    src={selectedCard.cardImageUrl}
                    alt={selectedCard.cardName}
                    fallback={
                      <div className={styles.cardPlaceholder}>
                        <span className={styles.cardPlaceholderText}>{selectedCard.cardIssuerName}</span>
                      </div>
                    }
                  />
                </div>
                <div className={styles.detailInfo}>
                  <span className={styles.detailName}>{selectedCard.cardName}</span>
                  <span className={styles.detailMeta}>{selectedCard.cardIssuerName}</span>
                  <span className={styles.detailMeta}>{selectedCard.cardDescription}</span>
                  {selectedCard.activeBenefits.length > 0 && (
                    <div className={styles.benefits}>
                      {selectedCard.activeBenefits.map((b) => (
                        <div key={b.categoryId} className={styles.benefitRow}>
                          <span className={styles.benefitName}>{b.categoryName}</span>
                          <span className={styles.benefitRate}>{(b.discountRate * 100).toFixed(0)}% 할인</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <button className={styles.applyButton} onClick={() => onApply(selectedCard)}>
                  카드 신청하기
                </button>
              </div>
            ) : (
              /* 카드 그리드 */
              <div className={styles.grid}>
                {recommendations.map((card) => (
                  <div
                    key={card.cardProductId}
                    className={styles.gridCard}
                    onClick={() => setSelectedCard(card)}
                  >
                    <div className={styles.cardImage}>
                      <AuthImage
                        src={card.cardImageUrl}
                        alt={card.cardName}
                        fallback={
                          <div className={styles.cardPlaceholder}>
                            <span className={styles.cardPlaceholderText}>{card.cardIssuerName}</span>
                          </div>
                        }
                      />
                    </div>
                    <span className={styles.gridCardName}>{card.cardName}</span>
                    <span className={styles.gridCardSummary}>{card.cardDescription}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 전체 카드 탭 */}
        {activeTab === 'my' && (
          <div className={styles.content}>
            {selectedCard ? (
              <div className={styles.detail}>
                <button className={styles.backButton} onClick={() => setSelectedCard(null)}>
                  ← 목록으로
                </button>
                <div className={styles.detailImageWrap}>
                  <AuthImage
                    src={selectedCard.cardImageUrl}
                    alt={selectedCard.cardName}
                    fallback={
                      <div className={styles.cardPlaceholder}>
                        <span className={styles.cardPlaceholderText}>{selectedCard.cardIssuerName}</span>
                      </div>
                    }
                  />
                </div>
                <div className={styles.detailInfo}>
                  <span className={styles.detailName}>{selectedCard.cardName}</span>
                  <span className={styles.detailMeta}>{selectedCard.cardIssuerName}</span>
                  <span className={styles.detailMeta}>{selectedCard.cardDescription}</span>
                  {selectedCard.activeBenefits.length > 0 && (
                    <div className={styles.benefits}>
                      {selectedCard.activeBenefits.map((b) => (
                        <div key={b.categoryId} className={styles.benefitRow}>
                          <span className={styles.benefitName}>{b.categoryName}</span>
                          <span className={styles.benefitRate}>{(b.discountRate * 100).toFixed(0)}% 할인</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
                <button className={styles.applyButton} onClick={() => onApply(selectedCard)}>
                  카드 신청하기
                </button>
              </div>
            ) : (
              <div className={styles.grid}>
                {allCards.map((card) => (
                  <div
                    key={card.cardProductId}
                    className={styles.gridCard}
                    onClick={() => setSelectedCard(card)}
                  >
                    <div className={styles.cardImage}>
                      <AuthImage
                        src={card.cardImageUrl}
                        alt={card.cardName}
                        fallback={
                          <div className={styles.cardPlaceholder}>
                            <span className={styles.cardPlaceholderText}>{card.cardIssuerName}</span>
                          </div>
                        }
                      />
                    </div>
                    <span className={styles.gridCardName}>{card.cardName}</span>
                    <span className={styles.gridCardSummary}>{card.cardDescription}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
