import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { JobOfferList } from '../entities/JobOffer';
import type { ICareerRepository } from '../repositories/ICareerRepository';

@injectable()
export class GetCareerJobOffersUseCase {
  private readonly repository: ICareerRepository;

  constructor(
    @inject(DI_TOKENS.ICareerRepository)
    repository: ICareerRepository,
  ) {
    this.repository = repository;
  }

  async execute(sessionId: number): Promise<JobOfferList> {
    return this.repository.getJobOffers(sessionId);
  }
}
