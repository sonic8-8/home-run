import type { IPassRepository } from '../repositories/IPassRepository';
import type { PassHistoryPage } from '../entities/PassHistory';

export class GetPassHistoryUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(page = 0, size = 10): Promise<PassHistoryPage> {
    return this.repository.getHistory(page, size);
  }
}
