import type { IGameWorldRepository } from '@features/game/domain/repositories/IGameWorldRepository'
import type { TurnNews } from '@features/game/domain/entities/GameTurn'

export class GetLatestNewsUseCase {
  private readonly repository: IGameWorldRepository

  constructor(repository: IGameWorldRepository) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<TurnNews> {
    return this.repository.getLatestNews(sessionId)
  }
}
