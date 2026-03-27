import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { IGameWorldRepository } from '@features/game/domain/repositories/IGameWorldRepository'
import type { GameTurn } from '@features/game/domain/entities/GameTurn'

@injectable()
export class GetGameTurnUseCase {
  private readonly repository: IGameWorldRepository

  constructor(
    @inject(DI_TOKENS.IGameWorldRepository)
    repository: IGameWorldRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<GameTurn> {
    return this.repository.getTurn(sessionId)
  }
}
