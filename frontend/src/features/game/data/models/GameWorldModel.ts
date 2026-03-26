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
