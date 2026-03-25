import type { IPassRepository } from '../repositories/IPassRepository';
import type { PassSaveResult } from '../entities/PassSaveResult';

export class SavePassUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(subscriptionId: number): Promise<PassSaveResult> {
    return this.repository.save(subscriptionId);
  }
}
