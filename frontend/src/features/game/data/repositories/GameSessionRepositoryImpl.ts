import { inject, injectable } from 'tsyringe';
import type { IGameSessionRepository } from '../../domain/repositories/IGameSessionRepository';
import type { GameSlot } from '../../domain/entities/GameSlot';
import type { GameSlotModel } from '../models/GameSessionModel';
import { GameSessionRemoteDataSource } from '../datasources/GameSessionRemoteDataSource';

const SLOT_STATUSES: ReadonlySet<GameSlot['status']> = new Set([
  'EMPTY',
  'IN_PROGRESS',
  'CLEAR',
  'BANKRUPT',
  'TIMEOUT',
  'FORECLOSURE',
]);

const JOB_TYPES: ReadonlySet<NonNullable<GameSlot['jobType']>> = new Set([
  'LARGE_BIZ',
  'MID_BIZ',
  'SMALL_BIZ',
  'STARTUP',
  'FREELANCER',
]);

@injectable()
export class GameSessionRepositoryImpl implements IGameSessionRepository {
  private readonly dataSource: GameSessionRemoteDataSource;

  constructor(
    @inject(GameSessionRemoteDataSource)
    dataSource: GameSessionRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async getSlots(): Promise<GameSlot[]> {
    const response = await this.dataSource.getSlots();
    return response.sessions.map((model) => this.toEntity(model));
  }

  private toEntity(model: GameSlotModel): GameSlot {
    return {
      slotNumber: model.slotNumber as 1 | 2 | 3,
      sessionId: model.sessionId,
      status: this.toSlotStatus(model.status),
      characterName: model.characterName,
      jobType: this.toJobType(model.jobType),
      totalAssets: model.totalAssets,
      createdAt: model.createdAt,
      currentTurn: model.currentTurn,
    };
  }

  private toSlotStatus(status: GameSlotModel['status']): GameSlot['status'] {
    if (SLOT_STATUSES.has(status)) {
      return status;
    }
    throw new Error(`지원하지 않는 세션 상태입니다: ${status}`);
  }

  private toJobType(jobType: GameSlotModel['jobType']): GameSlot['jobType'] {
    if (jobType === undefined) {
      return undefined;
    }
    if (JOB_TYPES.has(jobType as NonNullable<GameSlot['jobType']>)) {
      return jobType as NonNullable<GameSlot['jobType']>;
    }
    return undefined;
  }
}
