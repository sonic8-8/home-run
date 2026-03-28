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
