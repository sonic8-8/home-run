import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type { ResolvedGameEvent } from '@features/game/domain/entities/GameTurn'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'

@injectable()
export class ResolveGameEventUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(
    sessionId: number,
    eventId: number,
    choiceId: number | null,
  ): Promise<ResolvedGameEvent> {
    return this.repository.resolveEvent(sessionId, eventId, choiceId)
  }
}
