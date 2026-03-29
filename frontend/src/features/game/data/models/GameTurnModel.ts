export interface EconomicCycleModel {
  phase: string
  description: string
}

export interface GameTurnResponseModel {
  turnNumber: number
  currentDate: string
  month: number
  economicCycle: EconomicCycleModel
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
