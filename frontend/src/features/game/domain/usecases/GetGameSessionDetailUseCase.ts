import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { GameSessionDetail } from '../entities/GameSlot';
import type { IGameSessionRepository } from '../repositories/IGameSessionRepository';

@injectable()
export class GetGameSessionDetailUseCase {
  private readonly repository: IGameSessionRepository;

  constructor(
    @inject(DI_TOKENS.IGameSessionRepository)
    repository: IGameSessionRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number): Promise<GameSessionDetail> {
    return this.repository.getSessionDetail(sessionId);
  }
}
