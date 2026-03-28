import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'
import type { TurnNews } from '@features/game/domain/entities/GameTurn'

@injectable()
export class GetLatestNewsUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<TurnNews> {
    return this.repository.getLatestNews(sessionId)
  }
}
