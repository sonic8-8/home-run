// import { injectable, inject } from 'tsyringe';
import type { IGameSessionRepository } from '../repositories/IGameSessionRepository';
import type { GameSlot } from '../entities/GameSlot';

// @injectable()
export class GetGameSlotsUseCase {
  private readonly repository: IGameSessionRepository;

  constructor(repository: IGameSessionRepository) {
    this.repository = repository;
  }

  async execute(): Promise<GameSlot[]> {
    return this.repository.getSlots();
  }
}
