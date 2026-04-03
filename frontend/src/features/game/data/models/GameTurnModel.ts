export interface EconomicCycleModel {
  phase: string
  description: string
}

export interface GameTurnAssetSnapshotModel {
  cashBalance: number
  netWorth: number
}

export interface GameTurnStatSnapshotModel {
  health: number
  fatigue: number
  stress: number
  happiness: number
  knowledge: number
}

export interface GameTurnRuntimeSnapshotModel {
  assets: GameTurnAssetSnapshotModel
  stats: GameTurnStatSnapshotModel
}

export interface GameTurnResponseModel {
  turnNumber: number
  currentDate: string
  month: number
  economicCycle: EconomicCycleModel
  runtimeSnapshot: GameTurnRuntimeSnapshotModel
}

export interface TurnActionEffectModel {
  cash: number
  health: number
  fatigue: number
  stress: number
  happiness: number
  knowledge: number
}

export interface TurnActionModel {
  actionType: string
  label: string
  iconUrl: string
  effects: TurnActionEffectModel
}

export interface TurnActionsResponseModel {
  shopping: TurnActionModel[]
  activities: TurnActionModel[]
}

export interface NewsItemModel {
  newsId: string
  headline: string
  content: string
  sourceName: string
  publishedDate: string
  economicCycleType: string
}

export interface LatestTurnNewsResponseModel {
  turnNumber: number
  currentDate: string
  news: NewsItemModel[]
}

export interface NewsHistoryItemModel {
  turnNumber: number
  newsId: string
  headline: string
  publishedDate: string
}

export interface GameNewsHistoryResponseModel {
  newsHistories: NewsHistoryItemModel[]
}

export interface PendingEventChoiceModel {
  choiceId: number
  choiceCode: string
  choiceName: string
  description: string
}

export interface PendingEventModel {
  eventId: number
  type: string
  title: string
  description: string
  imageUrl: string | null
  choices: PendingEventChoiceModel[] | null
  sender: string | null
  receiver: string | null
  date: string | null
  offeredSalary: number | null
  currentSalary: number | null
}

export interface PendingEventsResponseModel {
  events: PendingEventModel[]
}

export interface ResolvedEventEffectModel {
  effectOrder: number
  applicationTimingType: string
  targetTableName: string | null
  targetColumnName: string | null
  operationType: string
  baseNumberValue: number | null
  minNumberValue: number | null
  maxNumberValue: number | null
  baseTextValue: string | null
  durationTurns: number | null
  note: string | null
}

export interface ResolveEventResponseModel {
  eventId: number
  gameEventId: number
  choiceId: number | null
  selectedChoiceCode: string | null
  resultEffects: ResolvedEventEffectModel[]
  resultSummary: string
}

export interface TurnStatChangesModel {
  health: number
  fatigue: number
  stress: number
  happiness: number
  knowledge: number
}

export interface TurnPreviewSlotModel {
  slotIndex: number
  actionType: string
  forcedAction: boolean
}

export interface TurnPreviewResponseModel {
  slots: TurnPreviewSlotModel[]
  previewCashChange: number
  previewStatChanges: TurnStatChangesModel
}

export interface TurnSettlementLogItemModel {
  phase: string
  description: string
  cashChange: number
  statChanges: TurnStatChangesModel
}

export interface TurnUpdatedAssetsModel {
  cash: number
  loan: number
  realEstateValue: number
  netAssets: number
}

export interface TurnCommitFlagsModel {
  isBankrupt: boolean
  isCleared: boolean
  isBurnout: boolean
  isForcedResignation: boolean
  hasEvent: boolean
}

export interface TurnCommitResponseModel {
  turnNumber: number
  settlementLog: TurnSettlementLogItemModel[]
  updatedAssets: TurnUpdatedAssetsModel
  statChanges: TurnStatChangesModel
  flags: TurnCommitFlagsModel
}
