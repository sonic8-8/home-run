import { useState } from 'react';
import type { CardRecommendation } from '../../../domain/entities/CardRecommendation';
import type { MyCard } from '../../../domain/entities/MyCard';
import { CardModal } from '../CardModal/CardModal';
import styles from './CardRecommendations.module.css';

interface CardRecommendationsProps {
  cards: CardRecommendation[];
  myCards: MyCard[];
}

export function CardRecommendations({ cards, myCards }: CardRecommendationsProps) {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.icon}>💳</span>
          <div>
            <div className={styles.title}>당신을 위한 카드 추천</div>
            <div className={styles.subtitle}>게임에서 본 그 카드 직접 확인해볼까요?</div>
          </div>
        </div>
        <div className={styles.list}>
          {cards.map((c) => (
            <div
              key={c.cardId}
              className={styles.item}
              onClick={() => setModalOpen(true)}
              style={{ cursor: 'pointer' }}
            >
              <div className={styles.cardImage}>
                {c.cardImageUrl ? (
                  <img src={c.cardImageUrl} alt={c.cardName} />
                ) : (
                  <div className={styles.cardPlaceholder}>
                    <span className={styles.cardPlaceholderText}>Samsung Card</span>
                  </div>
                )}
              </div>
              <div className={styles.cardName}>{c.cardName}</div>
            </div>
          ))}
        </div>
      </div>

      <CardModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        recommendations={cards}
        myCards={myCards}
        onApply={(card) => {
          console.log('카드 신청하기', card.cardId);
          // TODO: API 연동
        }}
        onCancel={(cardId) => {
          console.log('카드 해지하기', cardId);
          // TODO: API 연동
        }}
      />
    </>
  );
}
