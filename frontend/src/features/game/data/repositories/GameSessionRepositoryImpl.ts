import { inject, injectable } from 'tsyringe';
import type { CreateGameSessionInput } from '../../domain/entities/CreateGameSessionInput';
import type {
  GameSessionCreation,
  GameSessionDataSourceType,
} from '../../domain/entities/GameSessionCreation';
import type { IGameSessionRepository } from '../../domain/repositories/IGameSessionRepository';
import type { GameSlot } from '../../domain/entities/GameSlot';
import type {
  CreateGameSessionRequestModel,
  CreateGameSessionResponseModel,
  GameSlotModel,
} from '../models/GameSessionModel';
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

const SESSION_DATA_SOURCE_TYPES: ReadonlySet<GameSessionDataSourceType> = new Set([
  'MY_DATA',
  'PROFILE',
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

  async createSession(
    input: CreateGameSessionInput,
  ): Promise<GameSessionCreation> {
    const response = await this.dataSource.createSession(
      this.toCreateRequestModel(input),
    );
    return this.toGameSessionCreation(response);
  }

  async getSlots(): Promise<GameSlot[]> {
    const response = await this.dataSource.getSlots();
    return response.sessions.map((model) => this.toEntity(model));
  }

  private toCreateRequestModel(
    input: CreateGameSessionInput,
  ): CreateGameSessionRequestModel {
    const requestModel: CreateGameSessionRequestModel = {
      slotNumber: input.slotNumber,
      characterType: input.characterType,
      characterName: input.characterName,
      regionCode: input.regionCode,
      districtCode: input.districtCode,
      targetPropertyId: input.targetPropertyId,
      useMyData: input.useMyData,
    };

    if (!input.useMyData) {
      requestModel.jobType = input.jobType;
    }

    return requestModel;
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

  private toGameSessionCreation(
    model: CreateGameSessionResponseModel,
  ): GameSessionCreation {
    return {
      sessionId: model.sessionId,
      slotNumber: model.slotNumber as 1 | 2 | 3,
      status: this.toCreatedSessionStatus(model.sessionStatus),
      currentTurn: model.currentTurn,
      dataSourceType: this.toDataSourceType(model.dataSourceType),
    };
  }

  private toSlotStatus(status: GameSlotModel['status']): GameSlot['status'] {
    if (SLOT_STATUSES.has(status)) {
      return status;
    }
    throw new Error(`지원하지 않는 세션 상태입니다: ${status}`);
  }

  private toCreatedSessionStatus(
    status: CreateGameSessionResponseModel['sessionStatus'],
  ): GameSessionCreation['status'] {
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

  private toDataSourceType(
    dataSourceType: CreateGameSessionResponseModel['dataSourceType'],
  ): GameSessionDataSourceType {
    if (SESSION_DATA_SOURCE_TYPES.has(dataSourceType)) {
      return dataSourceType;
    }
    throw new Error(`지원하지 않는 데이터 소스 타입입니다: ${dataSourceType}`);
  }
}
