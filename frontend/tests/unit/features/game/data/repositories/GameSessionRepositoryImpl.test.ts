import { beforeEach, describe, expect, it, vi } from 'vitest';
import type {
  CreateGameSessionRequestModel,
  CreateGameSessionResponseModel,
  GameSessionsResponseModel,
} from '@features/game/data/models/GameSessionModel';
import { GameSessionRemoteDataSource } from '@features/game/data/datasources/GameSessionRemoteDataSource';
import { GameSessionRepositoryImpl } from '@features/game/data/repositories/GameSessionRepositoryImpl';

describe('GameSessionRepositoryImpl', () => {
  let repository: GameSessionRepositoryImpl;
  let dataSource: GameSessionRemoteDataSource;

  beforeEach(() => {
    dataSource = {
      createSession: vi.fn<(_: CreateGameSessionRequestModel) => Promise<CreateGameSessionResponseModel>>(),
      getSlots: vi.fn<() => Promise<GameSessionsResponseModel>>(),
    } as unknown as GameSessionRemoteDataSource;
    repository = new GameSessionRepositoryImpl(dataSource);
  });

  it('omits jobType when creating a MY_DATA session', async () => {
    vi.mocked(dataSource.createSession).mockResolvedValue({
      sessionId: 31,
      slotNumber: 3,
      sessionStatus: 'IN_PROGRESS',
      currentTurn: 1,
      dataSourceType: 'MY_DATA',
    });

    await repository.createSession({
      slotNumber: 3,
      characterType: 'FEMALE',
      characterName: '테스터',
      regionCode: '11',
      districtCode: '11680',
      targetPropertyId: 101,
      useMyData: true,
    });

    expect(dataSource.createSession).toHaveBeenCalledWith({
      slotNumber: 3,
      characterType: 'FEMALE',
      characterName: '테스터',
      regionCode: '11',
      districtCode: '11680',
      targetPropertyId: 101,
      useMyData: true,
    });
  });

  it('includes jobType when creating a PROFILE session', async () => {
    vi.mocked(dataSource.createSession).mockResolvedValue({
      sessionId: 32,
      slotNumber: 2,
      sessionStatus: 'IN_PROGRESS',
      currentTurn: 1,
      dataSourceType: 'PROFILE',
    });

    await repository.createSession({
      slotNumber: 2,
      characterType: 'MALE',
      characterName: '홍길동',
      jobType: 'STARTUP',
      regionCode: '26',
      districtCode: '26110',
      targetPropertyId: 202,
      useMyData: false,
    });

    expect(dataSource.createSession).toHaveBeenCalledWith({
      slotNumber: 2,
      characterType: 'MALE',
      characterName: '홍길동',
      jobType: 'STARTUP',
      regionCode: '26',
      districtCode: '26110',
      targetPropertyId: 202,
      useMyData: false,
    });
  });
});
