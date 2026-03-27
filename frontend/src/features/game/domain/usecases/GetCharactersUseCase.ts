import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { CharacterOption } from '../entities/CharacterOption';

@injectable()
export class GetCharactersUseCase {
  private readonly repository: IGameInitRepository;

  constructor(
    @inject(DI_TOKENS.IGameInitRepository)
    repository: IGameInitRepository,
  ) {
    this.repository = repository;
  }

  execute(): Promise<CharacterOption[]> {
    return this.repository.getCharacters();
  }
}
