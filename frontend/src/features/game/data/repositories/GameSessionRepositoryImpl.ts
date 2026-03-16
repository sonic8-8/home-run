// import { injectable, inject } from 'tsyringe';
import type { IGameSessionRepository } from '../../domain/repositories/IGameSessionRepository';
import type { GameSessionRemoteDataSource } from '../datasources/GameSessionRemoteDataSource';
import type { GameSlot } from '../../domain/entities/GameSlot';
import type { GameSlotModel } from '../models/GameSessionModel';


// @injectable()
export class GameSessionRepositoryImpl implements IGameSessionRepository {
  private readonly dataSource: GameSessionRemoteDataSource;

  constructor(dataSource: GameSessionRemoteDataSource) {
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
      status: model.status,
      characterName: model.characterName,
      jobType: model.jobType as GameSlot['jobType'],
      totalAssets: model.totalAssets,
      createdAt: model.createdAt,
      currentTurn: model.currentTurn,
    };
  }
}
