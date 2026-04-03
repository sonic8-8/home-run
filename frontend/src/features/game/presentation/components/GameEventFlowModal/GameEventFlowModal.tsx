import { formatIsoDate } from '@shared/utils/formatter';
import type {
  PendingGameEvent,
  ResolvedGameEvent,
} from '@features/game/domain/entities/GameTurn';
import {
  EventCard,
  type EventCardModel,
} from '@features/game/presentation/components/EventCard';
import { JobChangeEventModal } from '@features/game/presentation/components/JobChangeEventModal';
import styles from './GameEventFlowModal.module.css';

interface GameEventFlowModalProps {
  isOpen: boolean;
  event: PendingGameEvent | null;
  resolvedEvent: ResolvedGameEvent | null;
  hasMoreEvents: boolean;
  isLoading: boolean;
  isResolving: boolean;
  error: string | null;
  onResolve: (choiceId: number | null) => void;
  onRetry: () => void;
  onContinue: () => void;
  onClose: () => void;
}

function buildEventCardModel(event: PendingGameEvent): EventCardModel {
  return {
    title: event.title,
    description: event.description,
    imageSrc: event.imageUrl ?? '',
    buttons:
      event.choices.length > 0
        ? event.choices.map((choice, index) => ({
            actionId: String(choice.choiceId),
            label: choice.choiceName,
            variant: index === 0 ? 'primary' : 'secondary',
          }))
        : [
            {
              actionId: 'confirm',
              label: '확인',
              variant: 'primary',
            },
          ],
  };
}

function renderMetaLabel(event: PendingGameEvent): string {
  switch (event.type) {
    case 'PHONE':
      return '통화';
    case 'LETTER':
      return '편지';
    case 'GIFT':
      return '선물';
    case 'JOB_TRANSFER':
      return '이직 제안';
    default:
      return '이벤트';
  }
}

export function GameEventFlowModal({
  isOpen,
  event,
  resolvedEvent,
  hasMoreEvents,
  isLoading,
  isResolving,
  error,
  onResolve,
  onRetry,
  onContinue,
  onClose,
}: GameEventFlowModalProps) {
  if (!isOpen) {
    return null;
  }

  if (
    event?.type === 'JOB_TRANSFER' &&
    event.choices.length >= 2 &&
    resolvedEvent === null &&
    !isLoading &&
    error === null
  ) {
    const [approveChoice, declineChoice] = event.choices;

    return (
      <JobChangeEventModal
        isOpen
        onClose={onClose}
        onApprove={() => onResolve(approveChoice.choiceId)}
        onDecline={() => onResolve(declineChoice.choiceId)}
        senderName={event.sender ?? '회사 인사팀'}
        recipientName={event.receiver ?? '플레이어'}
        date={event.date === null ? '-' : formatIsoDate(event.date)}
        companyName={event.sender ?? '회사'}
        currentSalary={event.currentSalary ?? 0}
        offeredSalary={event.offeredSalary ?? 0}
        subject={event.title}
        description={event.description}
        approveLabel={approveChoice.choiceName}
        declineLabel={declineChoice.choiceName}
      />
    );
  }

  const eventCard = event === null ? null : buildEventCardModel(event);
  const effectNotes = resolvedEvent?.resultEffects
    .map((effect) => effect.note)
    .filter((note): note is string => note !== null && note.length > 0) ?? [];

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div
        className={styles.modal}
        onClick={(modalEvent) => modalEvent.stopPropagation()}
        data-guide="game-event-modal"
      >
        {isLoading ? (
          <div className={styles.statusPanel}>
            <strong>이벤트를 불러오는 중입니다.</strong>
            <p>정산 결과와 연결된 이벤트를 확인하고 있습니다.</p>
          </div>
        ) : error && event === null && resolvedEvent === null ? (
          <div className={styles.statusPanel}>
            <strong>이벤트를 열지 못했습니다.</strong>
            <p>{error}</p>
            <div className={styles.footer}>
              <button type="button" className={styles.secondaryButton} onClick={onClose}>
                닫기
              </button>
              <button type="button" className={styles.primaryButton} onClick={onRetry}>
                다시 시도
              </button>
            </div>
          </div>
        ) : resolvedEvent !== null ? (
          <div className={styles.resultPanel}>
            <div className={styles.resultHeader}>
              <span className={styles.resultEyebrow}>이벤트 처리 완료</span>
              <h2 className={styles.resultTitle}>처리 결과를 반영했습니다.</h2>
            </div>
            {error && <div className={styles.inlineError}>{error}</div>}
            <p className={styles.resultSummary}>{resolvedEvent.resultSummary}</p>
            {effectNotes.length > 0 && (
              <ul className={styles.effectList}>
                {effectNotes.map((note) => (
                  <li key={note} className={styles.effectItem}>
                    {note}
                  </li>
                ))}
              </ul>
            )}
            <div className={styles.footer}>
              <button type="button" className={styles.primaryButton} onClick={onContinue}>
                {hasMoreEvents ? '다음 이벤트 확인' : '확인'}
              </button>
            </div>
          </div>
        ) : eventCard !== null && event !== null ? (
          <>
            <div className={styles.header}>
              <div>
                <span className={styles.eventBadge}>{renderMetaLabel(event)}</span>
                <h2 className={styles.title}>이번 달 이벤트</h2>
              </div>
              <button type="button" className={styles.closeButton} onClick={onClose}>
                닫기
              </button>
            </div>
            <div className={styles.metaCard}>
              {event.sender && (
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>보낸 사람</span>
                  <strong>{event.sender}</strong>
                </div>
              )}
              {event.receiver && (
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>받는 사람</span>
                  <strong>{event.receiver}</strong>
                </div>
              )}
              {event.date && (
                <div className={styles.metaRow}>
                  <span className={styles.metaLabel}>발생 일자</span>
                  <strong>{formatIsoDate(event.date)}</strong>
                </div>
              )}
            </div>
            {(event.currentSalary !== null || event.offeredSalary !== null) && (
              <div className={styles.salaryCard}>
                {event.currentSalary !== null && (
                  <div className={styles.salaryRow}>
                    <span>현재 연봉</span>
                    <strong>{event.currentSalary.toLocaleString('ko-KR')}원</strong>
                  </div>
                )}
                {event.offeredSalary !== null && (
                  <div className={styles.salaryRow}>
                    <span>제안 연봉</span>
                    <strong>{event.offeredSalary.toLocaleString('ko-KR')}원</strong>
                  </div>
                )}
              </div>
            )}
            {error && <div className={styles.inlineError}>{error}</div>}
            <div className={styles.cardShell}>
              <EventCard
                event={eventCard}
                onAction={(actionId) => {
                  if (isResolving) {
                    return;
                  }

                  if (event.choices.length === 0) {
                    onResolve(null);
                    return;
                  }

                  onResolve(Number(actionId));
                }}
              />
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
