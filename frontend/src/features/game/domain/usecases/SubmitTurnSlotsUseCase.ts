import { inject, injectable } from 'tsyringe'
import { DI_TOKENS } from '@core/di/tokens'
import type {
  TurnPreview,
  TurnSlotSelection,
} from '@features/game/domain/entities/GameTurn'
import type { IGameTurnRepository } from '@features/game/domain/repositories/IGameTurnRepository'

@injectable()
export class SubmitTurnSlotsUseCase {
  private readonly repository: IGameTurnRepository

  constructor(
    @inject(DI_TOKENS.IGameTurnRepository)
    repository: IGameTurnRepository,
  ) {
    this.repository = repository
  }

  async execute(
    sessionId: number,
    slots: readonly TurnSlotSelection[],
  ): Promise<TurnPreview> {
    return this.repository.submitTurnSlots(sessionId, slots)
  }
}
