import React, { useState } from 'react';
import clsx from 'clsx';
import styles from './EventCard.module.css';

export interface EventCardButton {
  readonly label: string;
  readonly variant: 'primary' | 'secondary';
  readonly actionId: string;
}

export interface EventCardModel {
  readonly title: string;
  readonly description: string;
  readonly imageSrc: string;
  readonly cardColor?: string;
  readonly buttons: readonly EventCardButton[];
}

interface EventCardProps {
  event: EventCardModel;
  onAction: (actionId: string) => void;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onAction }) => {
  const [failedImageSrc, setFailedImageSrc] = useState<string | null>(null);
  const shouldShowImage = event.imageSrc !== '' && failedImageSrc !== event.imageSrc;

  return (
    <div className={styles.card} style={{ backgroundColor: event.cardColor }}>
      {shouldShowImage ? (
        <img
          className={styles.image}
          src={event.imageSrc}
          alt={event.title}
          onError={() => setFailedImageSrc(event.imageSrc)}
        />
      ) : (
        <div className={styles.imageFallback} aria-label={`${event.title} 이미지 대체 영역`}>
          <span className={styles.imageFallbackBadge}>EVENT</span>
          <strong className={styles.imageFallbackTitle}>{event.title}</strong>
        </div>
      )}
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
