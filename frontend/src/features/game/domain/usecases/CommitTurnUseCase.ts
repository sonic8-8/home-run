import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { TurnCommitResult } from '@features/game/domain/entities/GameTurn'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'

@injectable()
export class CommitTurnUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(sessionId: number): Promise<TurnCommitResult> {
    return this.repository.commitTurn(sessionId)
  }
}
