import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { PassWidget } from './PassWidget';

vi.mock('../PassModal/PassModal', () => ({
  PassModal: ({
    isOpen,
  }: {
    isOpen: boolean;
  }) => (isOpen ? <div data-testid="pass-modal">modal</div> : null),
}));

describe('PassWidget', () => {
  it('renders recent pass history entries when history exists', () => {
    render(
      <PassWidget
        subscriptions={[
          {
            subscriptionId: 10,
            passId: 3,
            name: '커피 줄이기 PASS',
            amountPerSave: 5000,
            totalSaved: 15000,
            weeklyHistory: [true, false, true],
          },
        ]}
        history={{
          content: [
            {
              historyId: 1,
              passName: '커피 줄이기 PASS',
              amount: 5000,
              savedAt: '2026-03-28T00:00:00',
            },
            {
              historyId: 2,
              passName: '야식 줄이기 PASS',
              amount: 12000,
              savedAt: '2026-03-27T00:00:00',
            },
          ],
          page: 0,
          size: 10,
          totalElements: 2,
          totalPages: 1,
        }}
        allPasses={[]}
        onSave={vi.fn()}
        onUnsubscribe={vi.fn()}
        onSubscribe={vi.fn()}
      />,
    );

    expect(screen.getByTestId('pass-history-list')).toBeInTheDocument();
    expect(screen.getByText('최근 PASS 저축 기록')).toBeInTheDocument();
    expect(screen.getByText('커피 줄이기 PASS')).toBeInTheDocument();
    expect(screen.getByText('야식 줄이기 PASS')).toBeInTheDocument();
  });

  it('opens the pass modal from the widget actions', () => {
    render(
      <PassWidget
        subscriptions={[]}
        history={null}
        allPasses={[]}
        onSave={vi.fn()}
        onUnsubscribe={vi.fn()}
        onSubscribe={vi.fn()}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: '구독하러 가기' }));

    expect(screen.getByTestId('pass-modal')).toBeInTheDocument();
  });
});
