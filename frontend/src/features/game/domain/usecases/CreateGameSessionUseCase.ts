import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { CreateGameSessionInput } from '../entities/CreateGameSessionInput';
import type { GameSessionCreation } from '../entities/GameSessionCreation';
import type { IGameSessionRepository } from '../repositories/IGameSessionRepository';

@injectable()
export class CreateGameSessionUseCase {
  private readonly repository: IGameSessionRepository;

  constructor(
    @inject(DI_TOKENS.IGameSessionRepository)
    repository: IGameSessionRepository,
  ) {
    this.repository = repository;
  }

  async execute(input: CreateGameSessionInput): Promise<GameSessionCreation> {
    return this.repository.createSession(input);
  }
}
