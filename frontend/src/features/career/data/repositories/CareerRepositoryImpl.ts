import { inject, injectable } from 'tsyringe';
import { ResponseMappingError } from '@core/error/AppError';
import type {
  CareerSessionSlotModel,
  JobOfferListResponseModel,
  JobOfferModel,
  JobTransferResponseModel,
  SalaryNegotiationResponseModel,
} from '../models/CareerModel';
import { CareerRemoteDataSource } from '../datasources/CareerRemoteDataSource';
import type { ICareerRepository } from '../../domain/repositories/ICareerRepository';
import type { CareerJobType, CareerSession } from '../../domain/entities/CareerSession';
import type { JobOffer, JobOfferList } from '../../domain/entities/JobOffer';
import type { JobTransferResult } from '../../domain/entities/JobTransfer';
import type { SalaryNegotiationResult } from '../../domain/entities/SalaryNegotiation';

const JOB_TYPES: ReadonlySet<CareerJobType> = new Set([
  'LARGE_BIZ',
  'MID_BIZ',
  'SMALL_BIZ',
  'STARTUP',
  'FREELANCER',
]);

@injectable()
export class CareerRepositoryImpl implements ICareerRepository {
  private readonly dataSource: CareerRemoteDataSource;

  constructor(
    @inject(CareerRemoteDataSource)
    dataSource: CareerRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async getActiveSession(): Promise<CareerSession | null> {
    const response = await this.dataSource.getSessions();
    const activeSlot = response.sessions.find((session) => (
      session.status === 'IN_PROGRESS' && session.sessionId !== null
    ));

    if (activeSlot === undefined || activeSlot.sessionId === null) {
      return null;
    }

    return this.toSession(activeSlot, activeSlot.sessionId);
  }

  async negotiateSalary(sessionId: number): Promise<SalaryNegotiationResult> {
    const response = await this.dataSource.negotiateSalary(sessionId);
    return this.toSalaryNegotiationResult(response);
  }

  async getJobOffers(sessionId: number): Promise<JobOfferList> {
    const response = await this.dataSource.getJobOffers(sessionId);
    return this.toJobOfferList(response);
  }

  async transferJob(sessionId: number, offerId: string): Promise<JobTransferResult> {
    const response = await this.dataSource.transferJob(sessionId, { offerId });
    return this.toJobTransferResult(response);
  }

  private toSession(
    model: CareerSessionSlotModel,
    sessionId: number,
  ): CareerSession {
    return {
      sessionId,
      slotNumber: model.slotNumber,
      characterName: model.characterName ?? null,
      jobType: this.toOptionalJobType(model.jobType),
      currentTurn: model.currentTurn ?? null,
    };
  }

  private toSalaryNegotiationResult(
    model: SalaryNegotiationResponseModel,
  ): SalaryNegotiationResult {
    return {
      success: model.success,
      previousSalary: model.previousSalary,
      newSalary: model.newSalary,
      raiseRate: model.raiseRate,
      lastNegotiatedTurn: model.lastNegotiatedTurn,
      message: model.message,
    };
  }

  private toJobOfferList(model: JobOfferListResponseModel): JobOfferList {
    return {
      offers: model.offers.map((offer) => this.toJobOffer(offer)),
      offerChanceBonusRate: model.offerChanceBonusRate,
      meetFriendBonusApplied: model.meetFriendBonusApplied,
    };
  }

  private toJobOffer(model: JobOfferModel): JobOffer {
    return {
      offerId: model.offerId,
      jobType: this.toJobType(model.jobType),
      companyName: model.companyName,
      currentSalary: model.currentSalary,
      offeredSalary: model.offeredSalary,
      probationTurns: model.probationTurns,
    };
  }

  private toJobTransferResult(model: JobTransferResponseModel): JobTransferResult {
    return {
      previousJobType: this.toJobType(model.previousJobType),
      newJobType: this.toJobType(model.newJobType),
      newJobTitle: model.newJobTitle,
      newSalary: model.newSalary,
      probationEndTurn: model.probationEndTurn,
      tenureReset: model.tenureReset,
      message: model.message,
    };
  }

  private toOptionalJobType(jobType: string | undefined): CareerJobType | null {
    if (jobType === undefined) {
      return null;
    }

    return this.toJobType(jobType);
  }

  private toJobType(jobType: string): CareerJobType {
    if (JOB_TYPES.has(jobType as CareerJobType)) {
      return jobType as CareerJobType;
    }

    throw new ResponseMappingError(`지원하지 않는 직업 유형입니다: ${jobType}`);
  }
}
