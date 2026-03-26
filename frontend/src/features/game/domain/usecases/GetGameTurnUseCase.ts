import type { IGameWorldRepository } from '@features/game/domain/repositories/IGameWorldRepository'
import type { GameTurn } from '@features/game/domain/entities/GameTurn'

export class GetGameTurnUseCase {
  private readonly repository: IGameWorldRepository

  constructor(repository: IGameWorldRepository) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<GameTurn> {
    return this.repository.getTurn(sessionId)
  }
}
