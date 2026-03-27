import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameSessionRepository } from '../repositories/IGameSessionRepository';
import type { GameSlot } from '../entities/GameSlot';

@injectable()
export class GetGameSlotsUseCase {
  private readonly repository: IGameSessionRepository;

  constructor(
    @inject(DI_TOKENS.IGameSessionRepository)
    repository: IGameSessionRepository,
  ) {
    this.repository = repository;
  }

  async execute(): Promise<GameSlot[]> {
    return this.repository.getSlots();
  }
}
