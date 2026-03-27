import { inject, injectable } from 'tsyringe'
import type { IGameWorldRepository } from '@features/game/domain/repositories/IGameWorldRepository'
import type { GameTurn, TurnNews, EconomicCyclePhase } from '@features/game/domain/entities/GameTurn'
import { GameWorldRemoteDataSource } from '@features/game/data/datasources/GameWorldRemoteDataSource'

@injectable()
export class GameWorldRepositoryImpl implements IGameWorldRepository {
  private readonly dataSource: GameWorldRemoteDataSource

  constructor(
    @inject(GameWorldRemoteDataSource)
    dataSource: GameWorldRemoteDataSource,
  ) {
    this.dataSource = dataSource
  }

  async getTurn(sessionId: number): Promise<GameTurn> {
    const model = await this.dataSource.getTurn(sessionId)
    return {
      turnNumber: model.turnNumber,
      currentDate: model.currentDate,
      month: model.month,
      economicCycle: {
        phase: model.economicCycle.phase as EconomicCyclePhase,
        description: model.economicCycle.description,
      },
    }
  }

  async getLatestNews(sessionId: number): Promise<TurnNews> {
    const model = await this.dataSource.getLatestNews(sessionId)
    return {
      turnNumber: model.turnNumber,
      currentDate: model.currentDate,
      news: model.news.map((n) => ({
        newsId: n.newsId,
        headline: n.headline,
        content: n.content,
        sourceName: n.sourceName,
        publishedDate: n.publishedDate,
        economicCycleType: n.economicCycleType,
      })),
    }
  }
}
