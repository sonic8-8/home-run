import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import fallbackActionIconUrl from '@assets/images/monthly/hobby.png'
import { MonthlyActivityModal } from './MonthlyActivityModal'

const shopping = [
  {
    actionType: 'GROCERY',
    label: '장보기',
    iconUrl: '/images/actions/grocery.png',
    effects: {
      cash: -30000,
      health: 0,
      fatigue: 0,
      stress: 0,
      happiness: 5,
      knowledge: 0,
    },
  },
]

const activities = [
  {
    actionType: 'STUDY',
    label: '공부',
    iconUrl: '/images/actions/study.png',
    effects: {
      cash: 0,
      health: 0,
      fatigue: 15,
      stress: 10,
      happiness: 0,
      knowledge: 15,
    },
  },
  {
    actionType: 'REST',
    label: '휴식',
    iconUrl: '/images/actions/rest.png',
    effects: {
      cash: 0,
      health: 0,
      fatigue: -30,
      stress: -20,
      happiness: 10,
      knowledge: 0,
    },
  },
]

describe('MonthlyActivityModal', () => {
  it('submits the selected action types in selection order', () => {
    const onSubmitSlots = vi.fn()

    render(
      <MonthlyActivityModal
        isOpen
        month={3}
        shopping={shopping}
        activities={activities}
        preview={null}
        commitResult={null}
        isActionsLoading={false}
        isSubmitting={false}
        isCommitting={false}
        error={null}
        onClose={vi.fn()}
        onSubmitSlots={onSubmitSlots}
        onBackToSelection={vi.fn()}
        onCommit={vi.fn()}
        onConfirmCommitResult={vi.fn()}
      />,
    )

    fireEvent.click(screen.getByText('장보기'))
    fireEvent.click(screen.getByText('공부'))
    fireEvent.click(screen.getByText('휴식'))
    fireEvent.click(screen.getByRole('button', { name: '미리보기 확인' }))

    expect(onSubmitSlots).toHaveBeenCalledWith(['GROCERY', 'STUDY', 'REST'])
  })

  it('delegates preview back and commit actions', () => {
    const onBackToSelection = vi.fn()
    const onCommit = vi.fn()

    render(
      <MonthlyActivityModal
        isOpen
        month={3}
        shopping={shopping}
        activities={activities}
        preview={{
          slots: [
            { slotIndex: 0, actionType: 'GROCERY', forcedAction: false },
            { slotIndex: 1, actionType: 'STUDY', forcedAction: false },
            { slotIndex: 2, actionType: 'REST', forcedAction: true },
          ],
          previewCashChange: -30000,
          previewStatChanges: {
            health: 0,
            fatigue: -15,
            stress: -10,
            happiness: 15,
            knowledge: 15,
          },
        }}
        commitResult={null}
        isActionsLoading={false}
        isSubmitting={false}
        isCommitting={false}
        error={null}
        onClose={vi.fn()}
        onSubmitSlots={vi.fn()}
        onBackToSelection={onBackToSelection}
        onCommit={onCommit}
        onConfirmCommitResult={vi.fn()}
      />,
    )

    fireEvent.click(screen.getByRole('button', { name: '다시 선택' }))
    fireEvent.click(screen.getByRole('button', { name: '턴 진행 확정' }))

    expect(onBackToSelection).toHaveBeenCalledTimes(1)
    expect(onCommit).toHaveBeenCalledTimes(1)
  })

  it('shows the commit result and delegates to the result confirm action', () => {
    const onConfirmCommitResult = vi.fn()

    render(
      <MonthlyActivityModal
        isOpen
        month={3}
        shopping={shopping}
        activities={activities}
        preview={null}
        commitResult={{
          turnNumber: 12,
          settlementLog: [
            {
              phase: 'ACTION_RESULT',
              description: '턴 행동 결과를 반영한다',
              cashChange: 430000,
              statChanges: {
                health: 3,
                fatigue: -14,
                stress: -8,
                happiness: 4,
                knowledge: 8,
              },
            },
          ],
          updatedAssets: {
            cash: 2820000,
            loan: 0,
            realEstateValue: 0,
            netAssets: 1820000,
          },
          statChanges: {
            health: 3,
            fatigue: -14,
            stress: -8,
            happiness: 4,
            knowledge: 8,
          },
          flags: {
            isBankrupt: false,
            isCleared: false,
            isBurnout: false,
            isForcedResignation: false,
            hasEvent: true,
          },
        }}
        isActionsLoading={false}
        isSubmitting={false}
        isCommitting={false}
        error={null}
        onClose={vi.fn()}
        onSubmitSlots={vi.fn()}
        onBackToSelection={vi.fn()}
        onCommit={vi.fn()}
        onConfirmCommitResult={onConfirmCommitResult}
      />,
    )

    expect(screen.getByText('12번째 턴 정산 완료')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: '이벤트 확인' }))

    expect(onConfirmCommitResult).toHaveBeenCalledTimes(1)
  })

  it('턴 이미지 로드 완료', () => {
    render(
      <MonthlyActivityModal
        isOpen
        month={3}
        shopping={shopping}
        activities={activities}
        preview={null}
        commitResult={null}
        isActionsLoading={false}
        isSubmitting={false}
        isCommitting={false}
        error={null}
        onClose={vi.fn()}
        onSubmitSlots={vi.fn()}
        onBackToSelection={vi.fn()}
        onCommit={vi.fn()}
        onConfirmCommitResult={vi.fn()}
      />,
    )

    const image = screen.getByAltText('공부')

    fireEvent.error(image)

    expect(image.getAttribute('src')).toBe(fallbackActionIconUrl)
  })
})
