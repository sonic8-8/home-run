import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'
import type { GameTurn } from '@features/game/domain/entities/GameTurn'

@injectable()
export class GetGameTurnUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<GameTurn> {
    return this.repository.getTurn(sessionId)
  }
}
