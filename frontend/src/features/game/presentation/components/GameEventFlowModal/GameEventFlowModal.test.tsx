import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { GameEventFlowModal } from './GameEventFlowModal';

const pendingEvent = {
  eventId: 301,
  type: 'CHOICE' as const,
  title: '가족 행사 초대',
  description: '주말에 참석할지 결정해야 합니다.',
  imageUrl: '/images/events/family.png',
  choices: [
    {
      choiceId: 701,
      choiceCode: 'A',
      choiceName: '참석한다',
      description: '가족 행사에 참석합니다.',
    },
    {
      choiceId: 702,
      choiceCode: 'B',
      choiceName: '불참한다',
      description: '일정을 지키기 위해 불참합니다.',
    },
  ],
  sender: '가족 단톡방',
  receiver: '김싸피',
  date: new Date('2026-05-01T00:00:00'),
  offeredSalary: null,
  currentSalary: null,
};

describe('GameEventFlowModal', () => {
  it('renders the pending event and resolves the selected choice', () => {
    const onResolve = vi.fn();

    render(
      <GameEventFlowModal
        isOpen
        event={pendingEvent}
        resolvedEvent={null}
        hasMoreEvents={false}
        isLoading={false}
        isResolving={false}
        error={null}
        onResolve={onResolve}
        onRetry={vi.fn()}
        onContinue={vi.fn()}
        onClose={vi.fn()}
      />,
    );

    expect(screen.getByText('이번 달 이벤트')).toBeInTheDocument();
    expect(screen.getByText('가족 행사 초대')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '참석한다' }));

    expect(onResolve).toHaveBeenCalledWith(701);
  });

  it('shows the resolved summary and continues the queue', () => {
    const onContinue = vi.fn();

    render(
      <GameEventFlowModal
        isOpen
        event={null}
        resolvedEvent={{
          eventId: 301,
          gameEventId: 1201,
          choiceId: 701,
          selectedChoiceCode: 'A',
          resultEffects: [
            {
              effectOrder: 1,
              applicationTimingType: 'IMMEDIATE',
              targetTableName: null,
              targetColumnName: null,
              operationType: 'NOTE',
              baseNumberValue: null,
              minNumberValue: null,
              maxNumberValue: null,
              baseTextValue: null,
              durationTurns: null,
              note: '가족과의 관계가 좋아졌습니다.',
            },
          ],
          resultSummary: '가족 행사에 참석했습니다.',
        }}
        hasMoreEvents
        isLoading={false}
        isResolving={false}
        error={null}
        onResolve={vi.fn()}
        onRetry={vi.fn()}
        onContinue={onContinue}
        onClose={vi.fn()}
      />,
    );

    expect(screen.getByText('가족 행사에 참석했습니다.')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다음 이벤트 확인' }));

    expect(onContinue).toHaveBeenCalledTimes(1);
  });
});
