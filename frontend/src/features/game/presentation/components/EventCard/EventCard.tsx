import React from 'react';
import clsx from 'clsx';
import type { GameEvent } from '@features/game/domain/entities/GameEvent';
import styles from './EventCard.module.css';

interface EventCardProps {
  event: GameEvent;
  onAction: (actionId: string) => void;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onAction }) => {
  return (
    <div className={styles.card} style={{ backgroundColor: event.cardColor }}>
      <img
        className={styles.image}
        src={event.imageSrc}
        alt={event.title}
      />
      <h2 className={styles.title}>{event.title}</h2>
      <p className={styles.description}>{event.description}</p>

      <div className={styles.buttonGroup}>
        {event.buttons.map((btn) => (
          <button
            key={btn.actionId}
            className={clsx(styles.button, styles[btn.variant])}
            onClick={() => onAction(btn.actionId)}
          >
            {btn.label}
          </button>
        ))}
      </div>
    </div>
  );
};
