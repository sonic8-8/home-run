import type { IGameInitRepository } from '../repositories/IGameInitRepository';
import type { CharacterOption } from '../entities/CharacterOption';

export class GetCharactersUseCase {
  private readonly repository: IGameInitRepository;
  constructor(repository: IGameInitRepository) { this.repository = repository; }

  execute(): Promise<CharacterOption[]> {
    return this.repository.getCharacters();
  }
}
