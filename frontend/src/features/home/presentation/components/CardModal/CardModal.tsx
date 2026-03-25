import { useState } from 'react';
import type { CardRecommendation } from '../../../domain/entities/CardRecommendation';
import type { MyCard } from '../../../domain/entities/MyCard';
import styles from './CardModal.module.css';

type Tab = 'recommend' | 'my';

interface CardModalProps {
  isOpen: boolean;
  onClose: () => void;
  recommendations: CardRecommendation[];
  myCards: MyCard[];
  onApply: (card: CardRecommendation) => void;
  onCancel: (cardId: string) => void;
}

export function CardModal({ isOpen, onClose, recommendations, myCards, onApply, onCancel }: CardModalProps) {
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
            내 카드
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
                  {selectedCard.cardImageUrl ? (
                    <img src={selectedCard.cardImageUrl} alt={selectedCard.cardName} />
                  ) : (
                    <div className={styles.cardPlaceholder}>
                      <span className={styles.cardPlaceholderText}>{selectedCard.cardIssuerName}</span>
                    </div>
                  )}
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
                      {card.cardImageUrl ? (
                        <img src={card.cardImageUrl} alt={card.cardName} />
                      ) : (
                        <div className={styles.cardPlaceholder}>
                          <span className={styles.cardPlaceholderText}>{card.cardIssuerName}</span>
                        </div>
                      )}
                    </div>
                    <span className={styles.gridCardName}>{card.cardName}</span>
                    <span className={styles.gridCardSummary}>{card.cardDescription}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* 내 카드 탭 */}
        {activeTab === 'my' && (
          <div className={styles.content}>
            {myCards.length === 0 ? (
              <p className={styles.emptyState}>보유 중인 카드가 없습니다.</p>
            ) : (
              <div className={styles.myCardList}>
                {myCards.map((card) => (
                  <div key={card.cardId} className={styles.myCardItem}>
                    <div className={styles.myCardImageWrap}>
                      {card.cardImageUrl ? (
                        <img src={card.cardImageUrl} alt={card.cardName} />
                      ) : (
                        <div className={styles.cardPlaceholder}>
                          <span className={styles.cardPlaceholderText}>{card.cardName}</span>
                        </div>
                      )}
                    </div>
                    <div className={styles.myCardInfo}>
                      <span className={styles.myCardName}>{card.cardName}</span>
                      <span className={styles.myCardMeta}>
                        **** **** **** {card.lastFourDigits} · {card.expiryDate}
                      </span>
                    </div>
                    <button className={styles.cancelButton} onClick={() => onCancel(card.cardId)}>
                      카드 해지하기
                    </button>
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
