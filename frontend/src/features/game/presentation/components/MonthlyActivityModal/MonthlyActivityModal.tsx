import { useEffect, useState, type SyntheticEvent } from 'react'
import clsx from 'clsx'
import fallbackActionIconUrl from '@assets/images/monthly/hobby.png'
import type {
  TurnCommitResult,
  TurnPreview,
  TurnStatChanges,
} from '@features/game/domain/entities/GameTurn'
import type { TurnAction } from '@features/game/domain/entities/TurnAction'
import { formatMoney } from '@shared/utils/formatter'
import styles from './MonthlyActivityModal.module.css'

const MAX_SLOTS = 3
const ITEMS_PER_PAGE = 3

const EFFECT_LABEL: Record<string, string> = {
  cash: '자금',
  health: '체력',
  knowledge: '지식',
  happiness: '행복',
  fatigue: '피로',
  stress: '스트레스',
}

function formatSignedValue(value: number): string {
  return value >= 0 ? `+${formatMoney(value)}` : formatMoney(value)
}

function replaceWithFallbackActionIcon(event: SyntheticEvent<HTMLImageElement>) {
  const image = event.currentTarget

  if (image.dataset.fallbackApplied === 'true') {
    return
  }

  image.dataset.fallbackApplied = 'true'
  image.src = fallbackActionIconUrl
}

interface MonthlyActivityModalProps {
  isOpen: boolean
  month: number
  shopping: readonly TurnAction[]
  activities: readonly TurnAction[]
  preview: TurnPreview | null
  commitResult: TurnCommitResult | null
  isActionsLoading: boolean
  isSubmitting: boolean
  isCommitting: boolean
  error: string | null
  onClose: () => void
  onSubmitSlots: (actionTypes: readonly string[]) => void
  onBackToSelection: () => void
  onCommit: () => void
  onConfirmCommitResult: () => void
  isResultConfirming?: boolean
}

interface ActionCardProps {
  action: TurnAction
  count: number
  totalSelected: number
  onAdd: () => void
  onRemove: () => void
}

function ActionCard({
  action,
  count,
  totalSelected,
  onAdd,
  onRemove,
}: ActionCardProps) {
  const isSelected = count > 0
  const canAdd = totalSelected < MAX_SLOTS

  return (
    <div
      className={clsx(styles.card, isSelected && styles.cardSelected)}
      onClick={() => {
        if (canAdd) {
          onAdd()
        }
      }}
      onKeyDown={(event) => {
        if ((event.key === 'Enter' || event.key === ' ') && canAdd) {
          event.preventDefault()
          onAdd()
        }
      }}
      role="button"
      tabIndex={0}
    >
      {isSelected && (
        <button
          type="button"
          className={styles.removeButton}
          onClick={(event) => {
            event.stopPropagation()
            onRemove()
          }}
        >
          ✕
        </button>
      )}
      {count > 1 && <span className={styles.countBadge}>{count}</span>}
      <img
        className={styles.cardIcon}
        src={action.iconUrl || fallbackActionIconUrl}
        alt={action.label}
        onError={replaceWithFallbackActionIcon}
      />
      <div className={styles.cardLabel}>{action.label}</div>
      <div className={styles.effectList}>
        {Object.entries(action.effects).map(([key, value]) => (
          <div key={key} className={styles.effectRow}>
            <span className={styles.effectLabel}>{EFFECT_LABEL[key] ?? key}</span>
            <span
              className={clsx(
                styles.effectValue,
                (value ?? 0) < 0 ? styles.negative : styles.positive,
              )}
            >
              {formatSignedValue(value ?? 0)}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

function StatSummary({ statChanges }: { statChanges: TurnStatChanges }) {
  return (
    <div className={styles.summaryGrid}>
      <div className={styles.summaryCard}>
        <span className={styles.summaryLabel}>체력</span>
        <span className={clsx(styles.summaryValue, statChanges.health < 0 ? styles.negative : styles.positive)}>
          {formatSignedValue(statChanges.health)}
        </span>
      </div>
      <div className={styles.summaryCard}>
        <span className={styles.summaryLabel}>피로</span>
        <span className={clsx(styles.summaryValue, statChanges.fatigue < 0 ? styles.negative : styles.positive)}>
          {formatSignedValue(statChanges.fatigue)}
        </span>
      </div>
      <div className={styles.summaryCard}>
        <span className={styles.summaryLabel}>스트레스</span>
        <span className={clsx(styles.summaryValue, statChanges.stress < 0 ? styles.negative : styles.positive)}>
          {formatSignedValue(statChanges.stress)}
        </span>
      </div>
      <div className={styles.summaryCard}>
        <span className={styles.summaryLabel}>행복</span>
        <span className={clsx(styles.summaryValue, statChanges.happiness < 0 ? styles.negative : styles.positive)}>
          {formatSignedValue(statChanges.happiness)}
        </span>
      </div>
      <div className={styles.summaryCard}>
        <span className={styles.summaryLabel}>지식</span>
        <span className={clsx(styles.summaryValue, statChanges.knowledge < 0 ? styles.negative : styles.positive)}>
          {formatSignedValue(statChanges.knowledge)}
        </span>
      </div>
    </div>
  )
}

export function MonthlyActivityModal({
  isOpen,
  month,
  shopping,
  activities,
  preview,
  commitResult,
  isActionsLoading,
  isSubmitting,
  isCommitting,
  error,
  onClose,
  onSubmitSlots,
  onBackToSelection,
  onCommit,
  onConfirmCommitResult,
  isResultConfirming = false,
}: MonthlyActivityModalProps) {
  const [shopPage, setShopPage] = useState(0)
  const [activityPage, setActivityPage] = useState(0)
  const [selectedActionTypes, setSelectedActionTypes] = useState<string[]>([])

  useEffect(() => {
    if (!isOpen) {
      setShopPage(0)
      setActivityPage(0)
      setSelectedActionTypes([])
    }
  }, [isOpen])

  if (!isOpen) {
    return null
  }

  const counts = selectedActionTypes.reduce<Record<string, number>>((next, actionType) => {
    next[actionType] = (next[actionType] ?? 0) + 1
    return next
  }, {})
  const totalSelected = selectedActionTypes.length
  const actions = [...shopping, ...activities]
  const shopSlice = shopping.slice(
    shopPage * ITEMS_PER_PAGE,
    shopPage * ITEMS_PER_PAGE + ITEMS_PER_PAGE,
  )
  const activitySlice = activities.slice(
    activityPage * ITEMS_PER_PAGE,
    activityPage * ITEMS_PER_PAGE + ITEMS_PER_PAGE,
  )
  const maxShopPage = Math.max(0, Math.ceil(shopping.length / ITEMS_PER_PAGE) - 1)
  const maxActivityPage = Math.max(0, Math.ceil(activities.length / ITEMS_PER_PAGE) - 1)

  const addAction = (actionType: string) => {
    if (selectedActionTypes.length >= MAX_SLOTS) {
      return
    }

    setSelectedActionTypes((prev) => [...prev, actionType])
  }

  const removeAction = (actionType: string) => {
    setSelectedActionTypes((prev) => {
      const targetIndex = prev.lastIndexOf(actionType)

      if (targetIndex < 0) {
        return prev
      }

      return prev.filter((_, index) => index !== targetIndex)
    })
  }

  const getActionLabel = (actionType: string): string =>
    actions.find((action) => action.actionType === actionType)?.label ?? actionType

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.header}>
          <div>
            <h2 className={styles.title}>{month}월 활동</h2>
            <p className={styles.subtitle}>
              이번 달 행동 3개를 고른 뒤 미리보기를 확인하고 턴을 확정하세요.
            </p>
          </div>
          <button type="button" className={styles.closeButton} onClick={onClose}>
            닫기
          </button>
        </div>

        {error && <div className={styles.errorBox}>{error}</div>}

        {commitResult !== null ? (
          <>
            <section className={styles.section}>
              <div className={styles.resultHeadline}>
                <strong>{commitResult.turnNumber}번째 턴 정산 완료</strong>
                {commitResult.flags.hasEvent && (
                  <span className={styles.noticeBadge}>이벤트 발생 예정</span>
                )}
              </div>
              <div className={styles.assetsGrid}>
                <div className={styles.assetCard}>
                  <span className={styles.assetLabel}>현금</span>
                  <strong>{formatMoney(commitResult.updatedAssets.cash)} 원</strong>
                </div>
                <div className={styles.assetCard}>
                  <span className={styles.assetLabel}>대출</span>
                  <strong>{formatMoney(commitResult.updatedAssets.loan)} 원</strong>
                </div>
                <div className={styles.assetCard}>
                  <span className={styles.assetLabel}>부동산 자산</span>
                  <strong>{formatMoney(commitResult.updatedAssets.realEstateValue)} 원</strong>
                </div>
                <div className={styles.assetCard}>
                  <span className={styles.assetLabel}>순자산</span>
                  <strong>{formatMoney(commitResult.updatedAssets.netAssets)} 원</strong>
                </div>
              </div>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>이번 달 스탯 변화</h3>
              <StatSummary statChanges={commitResult.statChanges} />
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>정산 로그</h3>
              <ul className={styles.logList}>
                {commitResult.settlementLog.map((item) => (
                  <li key={`${item.phase}-${item.description}`} className={styles.logItem}>
                    <div className={styles.logHeader}>
                      <strong>{item.description}</strong>
                      <span className={styles.logPhase}>{item.phase}</span>
                    </div>
                    <div className={styles.logCash}>
                      자금 변화 {formatSignedValue(item.cashChange)} 원
                    </div>
                    <StatSummary statChanges={item.statChanges} />
                  </li>
                ))}
              </ul>
            </section>

            <div className={styles.footer}>
              <button
                type="button"
                className={styles.primaryButton}
                onClick={onConfirmCommitResult}
                disabled={isResultConfirming}
              >
                {commitResult.flags.hasEvent ? '이벤트 확인' : '확인'}
              </button>
            </div>
          </>
        ) : preview !== null ? (
          <>
            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>선택한 슬롯</h3>
              <ul className={styles.previewList}>
                {preview.slots.map((slot) => (
                  <li key={`${slot.slotIndex}-${slot.actionType}`} className={styles.previewItem}>
                    <div>
                      <strong>{slot.slotIndex + 1}번 슬롯</strong>
                      <div className={styles.previewAction}>{getActionLabel(slot.actionType)}</div>
                    </div>
                    {slot.forcedAction && (
                      <span className={styles.noticeBadge}>강제 행동</span>
                    )}
                  </li>
                ))}
              </ul>
            </section>

            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>예상 변화</h3>
              <div className={styles.cashPreviewCard}>
                <span className={styles.summaryLabel}>예상 자금 변화</span>
                <strong className={preview.previewCashChange < 0 ? styles.negative : styles.positive}>
                  {formatSignedValue(preview.previewCashChange)} 원
                </strong>
              </div>
              <StatSummary statChanges={preview.previewStatChanges} />
            </section>

            <div className={styles.footer}>
              <button
                type="button"
                className={styles.secondaryButton}
                onClick={onBackToSelection}
              >
                다시 선택
              </button>
              <button
                type="button"
                className={styles.primaryButton}
                onClick={onCommit}
                disabled={isCommitting}
              >
                {isCommitting ? '턴 진행 중...' : '턴 진행 확정'}
              </button>
            </div>
          </>
        ) : (
          <>
            <div className={styles.slotCounter}>
              <span className={styles.slotCount}>{totalSelected}</span>
              <span className={styles.slotMax}>/ {MAX_SLOTS} 선택</span>
            </div>

            {isActionsLoading ? (
              <div className={styles.emptyState}>행동 목록을 불러오는 중입니다.</div>
            ) : actions.length === 0 ? (
              <div className={styles.emptyState}>선택 가능한 행동이 없습니다.</div>
            ) : (
              <>
                <section className={styles.category}>
                  <span className={styles.categoryLabel}>쇼핑</span>
                  <div className={styles.carousel}>
                    <button
                      type="button"
                      className={styles.arrowButton}
                      onClick={() => setShopPage((page) => Math.max(0, page - 1))}
                      disabled={shopPage === 0}
                    >
                      ◀
                    </button>
                    <div className={styles.cardRow}>
                      {shopSlice.map((action) => (
                        <ActionCard
                          key={action.actionType}
                          action={action}
                          count={counts[action.actionType] ?? 0}
                          totalSelected={totalSelected}
                          onAdd={() => addAction(action.actionType)}
                          onRemove={() => removeAction(action.actionType)}
                        />
                      ))}
                    </div>
                    <button
                      type="button"
                      className={styles.arrowButton}
                      onClick={() => setShopPage((page) => Math.min(maxShopPage, page + 1))}
                      disabled={shopPage >= maxShopPage}
                    >
                      ▶
                    </button>
                  </div>
                </section>

                <section className={styles.category}>
                  <span className={styles.categoryLabel}>활동</span>
                  <div className={styles.carousel}>
                    <button
                      type="button"
                      className={styles.arrowButton}
                      onClick={() => setActivityPage((page) => Math.max(0, page - 1))}
                      disabled={activityPage === 0}
                    >
                      ◀
                    </button>
                    <div className={styles.cardRow}>
                      {activitySlice.map((action) => (
                        <ActionCard
                          key={action.actionType}
                          action={action}
                          count={counts[action.actionType] ?? 0}
                          totalSelected={totalSelected}
                          onAdd={() => addAction(action.actionType)}
                          onRemove={() => removeAction(action.actionType)}
                        />
                      ))}
                    </div>
                    <button
                      type="button"
                      className={styles.arrowButton}
                      onClick={() => setActivityPage((page) => Math.min(maxActivityPage, page + 1))}
                      disabled={activityPage >= maxActivityPage}
                    >
                      ▶
                    </button>
                  </div>
                </section>
              </>
            )}

            <div className={styles.footer}>
              <button
                type="button"
                className={styles.primaryButton}
                onClick={() => onSubmitSlots(selectedActionTypes)}
                disabled={totalSelected !== MAX_SLOTS || isSubmitting || isActionsLoading}
              >
                {isSubmitting ? '미리보기 계산 중...' : '미리보기 확인'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
