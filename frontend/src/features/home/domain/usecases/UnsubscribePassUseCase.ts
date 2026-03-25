import type { IPassRepository } from '../repositories/IPassRepository';

export class UnsubscribePassUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(subscriptionId: number): Promise<void> {
    return this.repository.unsubscribe(subscriptionId);
  }
}
