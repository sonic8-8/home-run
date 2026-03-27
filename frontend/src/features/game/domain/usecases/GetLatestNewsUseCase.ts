import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { IGameWorldRepository } from '@features/game/domain/repositories/IGameWorldRepository'
import type { TurnNews } from '@features/game/domain/entities/GameTurn'

@injectable()
export class GetLatestNewsUseCase {
  private readonly repository: IGameWorldRepository

  constructor(
    @inject(DI_TOKENS.IGameWorldRepository)
    repository: IGameWorldRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<TurnNews> {
    return this.repository.getLatestNews(sessionId)
  }
}
