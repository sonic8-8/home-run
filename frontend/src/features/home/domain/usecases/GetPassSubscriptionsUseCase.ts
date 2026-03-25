import type { IPassRepository } from '../repositories/IPassRepository';
import type { PassSubscription } from '../entities/PassSubscription';

export class GetPassSubscriptionsUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(): Promise<PassSubscription[]> {
    return this.repository.getSubscriptions();
  }
}
