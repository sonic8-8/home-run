import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { NewsHistoryItem } from '@features/game/domain/entities/GameTurn'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'

@injectable()
export class GetNewsHistoryUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<readonly NewsHistoryItem[]> {
    return this.repository.getNewsHistory(sessionId)
  }
}
