export type EconomicCyclePhase = 'EXPANSION' | 'CONTRACTION' | 'RECOVERY' | 'PEAK' | 'TROUGH'

export interface EconomicCycle {
  readonly phase: EconomicCyclePhase
  readonly description: string
}

export interface GameTurn {
  readonly turnNumber: number
  readonly currentDate: string
  readonly month: number
  readonly economicCycle: EconomicCycle
}

export interface NewsItem {
  readonly newsId: string
  readonly headline: string
  readonly content: string
  readonly sourceName: string
  readonly publishedDate: string
  readonly economicCycleType: string
}

export interface TurnNews {
  readonly turnNumber: number
  readonly currentDate: string
  readonly news: NewsItem[]
}
