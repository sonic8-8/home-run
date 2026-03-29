import type { CareerSession } from '../entities/CareerSession';
import type { JobOfferList } from '../entities/JobOffer';
import type { JobTransferResult } from '../entities/JobTransfer';
import type { SalaryNegotiationResult } from '../entities/SalaryNegotiation';

export interface ICareerRepository {
  getActiveSession(): Promise<CareerSession | null>;
  negotiateSalary(sessionId: number): Promise<SalaryNegotiationResult>;
  getJobOffers(sessionId: number): Promise<JobOfferList>;
  transferJob(sessionId: number, offerId: string): Promise<JobTransferResult>;
}
