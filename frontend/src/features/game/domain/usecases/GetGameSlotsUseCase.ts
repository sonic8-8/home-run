import { injectable, inject } from 'tsyringe';
import type { IGameSessionRepository } from '../repositories/IGameSessionRepository';
import type { GameSlot } from '../entities/GameSlot';

@injectable()
export class GetGameSlotsUseCase {
  constructor(
    @inject('IGameSessionRepository')
    private readonly repository: IGameSessionRepository,
  ) {}

  async execute(): Promise<GameSlot[]> {
    return this.repository.getSlots();
  }
}
