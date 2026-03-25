import type { IPassRepository } from '../repositories/IPassRepository';
import type { PassWidgetData } from '../entities/PassWidgetData';

export class GetPassWidgetUseCase {
  private readonly repository: IPassRepository;
  constructor(repository: IPassRepository) { this.repository = repository; }

  execute(): Promise<PassWidgetData> {
    return this.repository.getWidget();
  }
}
