import type { IPassRepository } from '../repositories/IPassRepository';
import type { PassSubscribeResult } from '../entities/PassSubscribeResult';

export class SubscribePassUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(passId: number, sourceAccountId: string): Promise<PassSubscribeResult> {
    return this.repository.subscribe(passId, sourceAccountId);
  }
}
