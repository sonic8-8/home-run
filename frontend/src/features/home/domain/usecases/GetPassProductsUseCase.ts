import type { IPassRepository } from '../repositories/IPassRepository';
import type { Pass } from '../entities/Pass';

export class GetPassProductsUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(): Promise<Pass[]> {
    return this.repository.getProducts();
  }
}
