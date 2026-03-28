import type { GameTurn, TurnNews } from '@features/game/domain/entities/GameTurn'

export interface IGameTurnRepository {
  getTurn(sessionId: number): Promise<GameTurn>
  getLatestNews(sessionId: number): Promise<TurnNews>
}
