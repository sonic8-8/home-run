import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'
import type { TurnActions } from '@features/game/domain/entities/TurnAction'

@injectable()
export class GetTurnActionsUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<TurnActions> {
    return this.repository.getAvailableActions(sessionId)
  }
}
