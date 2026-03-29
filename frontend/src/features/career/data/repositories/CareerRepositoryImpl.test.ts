import { describe, expect, it, vi } from 'vitest';
import { ResponseMappingError } from '@core/error/AppError';
import type { CareerRemoteDataSource } from '../datasources/CareerRemoteDataSource';
import { CareerRepositoryImpl } from './CareerRepositoryImpl';

describe('CareerRepositoryImpl', () => {
  it('returns the first in-progress session as the active career session', async () => {
    const dataSource = {
      getSessions: vi.fn().mockResolvedValue({
        sessions: [
          { sessionId: null, slotNumber: 1, status: 'EMPTY' },
          {
            sessionId: 42,
            slotNumber: 2,
            characterName: '지민',
            jobType: 'STARTUP',
            currentTurn: 7,
            status: 'IN_PROGRESS',
          },
        ],
      }),
      negotiateSalary: vi.fn(),
      getJobOffers: vi.fn(),
      transferJob: vi.fn(),
    } as Pick<CareerRemoteDataSource, 'getSessions' | 'negotiateSalary' | 'getJobOffers' | 'transferJob'> as CareerRemoteDataSource;
    const repository = new CareerRepositoryImpl(dataSource);

    await expect(repository.getActiveSession()).resolves.toEqual({
      sessionId: 42,
      slotNumber: 2,
      characterName: '지민',
      jobType: 'STARTUP',
      currentTurn: 7,
    });
  });

  it('maps job offer metadata and offer entities', async () => {
    const dataSource = {
      getSessions: vi.fn(),
      negotiateSalary: vi.fn(),
      getJobOffers: vi.fn().mockResolvedValue({
        offers: [
          {
            offerId: 'offer-1',
            jobType: 'LARGE_BIZ',
            companyName: '홈런전자',
            currentSalary: 48000000,
            offeredSalary: 56000000,
            probationTurns: 2,
          },
        ],
        offerChanceBonusRate: 10,
        meetFriendBonusApplied: true,
      }),
      transferJob: vi.fn().mockResolvedValue({
        previousJobType: 'SMALL_BIZ',
        newJobType: 'LARGE_BIZ',
        newJobTitle: '플랫폼 PM',
        newSalary: 56000000,
        probationEndTurn: 12,
        tenureReset: true,
        message: '이직 성공',
      }),
    } as Pick<CareerRemoteDataSource, 'getSessions' | 'negotiateSalary' | 'getJobOffers' | 'transferJob'> as CareerRemoteDataSource;
    const repository = new CareerRepositoryImpl(dataSource);

    await expect(repository.getJobOffers(42)).resolves.toEqual({
      offers: [
        {
          offerId: 'offer-1',
          jobType: 'LARGE_BIZ',
          companyName: '홈런전자',
          currentSalary: 48000000,
          offeredSalary: 56000000,
          probationTurns: 2,
        },
      ],
      offerChanceBonusRate: 10,
      meetFriendBonusApplied: true,
    });

    await expect(repository.transferJob(42, 'offer-1')).resolves.toEqual({
      previousJobType: 'SMALL_BIZ',
      newJobType: 'LARGE_BIZ',
      newJobTitle: '플랫폼 PM',
      newSalary: 56000000,
      probationEndTurn: 12,
      tenureReset: true,
      message: '이직 성공',
    });
  });

  it('maps the salary negotiation response', async () => {
    const dataSource = {
      getSessions: vi.fn(),
      negotiateSalary: vi.fn().mockResolvedValue({
        success: true,
        previousSalary: 42000000,
        newSalary: 45700000,
        raiseRate: 9,
        lastNegotiatedTurn: 8,
        message: '협상 성공',
      }),
      getJobOffers: vi.fn(),
      transferJob: vi.fn(),
    } as Pick<CareerRemoteDataSource, 'getSessions' | 'negotiateSalary' | 'getJobOffers' | 'transferJob'> as CareerRemoteDataSource;
    const repository = new CareerRepositoryImpl(dataSource);

    await expect(repository.negotiateSalary(42)).resolves.toEqual({
      success: true,
      previousSalary: 42000000,
      newSalary: 45700000,
      raiseRate: 9,
      lastNegotiatedTurn: 8,
      message: '협상 성공',
    });
  });

  it('throws a mapping error when the backend returns an unknown job type', async () => {
    const dataSource = {
      getSessions: vi.fn().mockResolvedValue({
        sessions: [
          {
            sessionId: 88,
            slotNumber: 1,
            characterName: '유나',
            jobType: 'UNKNOWN_JOB',
            currentTurn: 4,
            status: 'IN_PROGRESS',
          },
        ],
      }),
      negotiateSalary: vi.fn(),
      getJobOffers: vi.fn(),
      transferJob: vi.fn(),
    } as Pick<CareerRemoteDataSource, 'getSessions' | 'negotiateSalary' | 'getJobOffers' | 'transferJob'> as CareerRemoteDataSource;
    const repository = new CareerRepositoryImpl(dataSource);

    await expect(repository.getActiveSession()).rejects.toBeInstanceOf(ResponseMappingError);
  });
});
