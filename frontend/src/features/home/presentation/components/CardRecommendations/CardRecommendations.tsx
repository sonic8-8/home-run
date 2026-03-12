import React from 'react';
import type { CardRecommendation } from '../../../domain/entities/CardRecommendation';
import styles from './CardRecommendations.module.css';

interface CardRecommendationsProps {
  cards: CardRecommendation[];
}

export const CardRecommendations: React.FC<CardRecommendationsProps> = ({ cards }) => {
  return (
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
          <a
            key={c.cardId}
            className={styles.item}
            href="https://www.samsungcard.com/home/card/cardinfo/PGHPPCCCardCardinfoDetails001?code=AAP1870&click=UHPPCO0101M0_card_PGHPPCCCardCardinfoDetails001_PRD_AAP1870"
            target="_blank"
            rel="noopener noreferrer"
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
          </a>
        ))}
      </div>
    </div>
  );
};
