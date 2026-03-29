export type EconomicCyclePhase = 'BOOM' | 'CRISIS' | 'RECOVERY'

export type EconomicCycleType =
  | 'BOOM_TO_BOOM'
  | 'BOOM_TO_CRISIS'
  | 'BOOM_TO_RECOVERY'
  | 'CRISIS_TO_CRISIS'
  | 'CRISIS_TO_RECOVERY'
  | 'CRISIS_TO_BOOM'
  | 'RECOVERY_TO_BOOM'
  | 'RECOVERY_TO_RECOVERY'
  | 'RECOVERY_TO_CRISIS'
  | 'EXPANSION'
  | 'CONTRACTION'
  | 'RECOVERY'
  | 'PEAK'
  | 'TROUGH'

export interface EconomicCycle {
  readonly phase: EconomicCyclePhase
  readonly description: string
}

export interface GameTurn {
  readonly turnNumber: number
  readonly currentDate: Date
  readonly month: number
  readonly economicCycle: EconomicCycle
}

export interface TurnStatChanges {
  readonly health: number
  readonly fatigue: number
  readonly stress: number
  readonly happiness: number
  readonly knowledge: number
}

export interface TurnSlotSelection {
  readonly slotIndex: number
  readonly actionType: string
}

export interface TurnPreviewSlot {
  readonly slotIndex: number
  readonly actionType: string
  readonly forcedAction: boolean
}

export interface TurnPreview {
  readonly slots: readonly TurnPreviewSlot[]
  readonly previewCashChange: number
  readonly previewStatChanges: TurnStatChanges
}

export interface TurnSettlementLogItem {
  readonly phase: string
  readonly description: string
  readonly cashChange: number
  readonly statChanges: TurnStatChanges
}

export interface TurnUpdatedAssets {
  readonly cash: number
  readonly loan: number
  readonly realEstateValue: number
  readonly netAssets: number
}

export interface TurnCommitFlags {
  readonly isBankrupt: boolean
  readonly isCleared: boolean
  readonly isBurnout: boolean
  readonly isForcedResignation: boolean
  readonly hasEvent: boolean
}

export interface TurnCommitResult {
  readonly turnNumber: number
  readonly settlementLog: readonly TurnSettlementLogItem[]
  readonly updatedAssets: TurnUpdatedAssets
  readonly statChanges: TurnStatChanges
  readonly flags: TurnCommitFlags
}

export interface NewsItem {
  readonly newsId: string
  readonly headline: string
  readonly content: string
  readonly sourceName: string
  readonly publishedDate: Date
  readonly economicCycleType: EconomicCycleType
}

export interface TurnNews {
  readonly turnNumber: number
  readonly currentDate: Date
  readonly news: readonly NewsItem[]
}
