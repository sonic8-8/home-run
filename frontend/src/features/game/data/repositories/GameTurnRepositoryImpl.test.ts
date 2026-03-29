import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ResponseMappingError } from '@core/error/AppError';
import type {
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
  TurnActionsResponseModel,
  TurnCommitResponseModel,
  TurnPreviewResponseModel,
} from '@features/game/data/models/GameTurnModel';
import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource';
import { GameTurnRepositoryImpl } from '@features/game/data/repositories/GameTurnRepositoryImpl';

describe('GameTurnRepositoryImpl', () => {
  let repository: GameTurnRepositoryImpl;
  let dataSource: GameTurnRemoteDataSource;

  beforeEach(() => {
    dataSource = {
      getTurn: vi.fn<(_: number) => Promise<GameTurnResponseModel>>(),
      getAvailableActions: vi.fn<(_: number) => Promise<TurnActionsResponseModel>>(),
      getLatestNews: vi.fn<(_: number) => Promise<LatestTurnNewsResponseModel>>(),
      submitTurnSlots: vi.fn<
        (_: number, request: { slots: { slotIndex: number; actionType: string }[] }) => Promise<TurnPreviewResponseModel>
      >(),
      commitTurn: vi.fn<(_: number) => Promise<TurnCommitResponseModel>>(),
    } as unknown as GameTurnRemoteDataSource;
    repository = new GameTurnRepositoryImpl(dataSource);
  });

  it('maps the turn state response to the game turn entity', async () => {
    vi.mocked(dataSource.getTurn).mockResolvedValue({
      turnNumber: 12,
      currentDate: '2026-01-01',
      month: 1,
      economicCycle: {
        phase: 'BOOM',
        description: '경기 호황기',
      },
    });

    const result = await repository.getTurn(101);

    expect(dataSource.getTurn).toHaveBeenCalledWith(101);
    expect(result).toEqual({
      turnNumber: 12,
      currentDate: new Date('2026-01-01T00:00:00'),
      month: 1,
      economicCycle: {
        phase: 'BOOM',
        description: '경기 호황기',
      },
    });
  });

  it('maps the latest news response to the turn news entity', async () => {
    vi.mocked(dataSource.getLatestNews).mockResolvedValue({
      turnNumber: 12,
      currentDate: '2026-01-01',
      news: [
        {
          newsId: 'news-1',
          headline: '부동산 시장 과열 경고',
          content: '시장 과열 신호가 확인됐다.',
          sourceName: '영남일보',
          publishedDate: '2026-01-01',
          economicCycleType: 'BOOM_TO_CRISIS',
        },
      ],
    });

    const result = await repository.getLatestNews(202);

    expect(dataSource.getLatestNews).toHaveBeenCalledWith(202);
    expect(result).toEqual({
      turnNumber: 12,
      currentDate: new Date('2026-01-01T00:00:00'),
      news: [
        {
          newsId: 'news-1',
          headline: '부동산 시장 과열 경고',
          content: '시장 과열 신호가 확인됐다.',
          sourceName: '영남일보',
          publishedDate: new Date('2026-01-01T00:00:00'),
          economicCycleType: 'BOOM_TO_CRISIS',
        },
      ],
    });
  });

  it('maps the available turn actions response', async () => {
    vi.mocked(dataSource.getAvailableActions).mockResolvedValue({
      shopping: [
        {
          actionType: 'GROCERY',
          label: '장보기',
          iconUrl: '/images/actions/grocery.png',
          effects: {
            cash: -30000,
            health: 0,
            fatigue: 0,
            stress: 0,
            happiness: 5,
            knowledge: 0,
          },
        },
      ],
      activities: [
        {
          actionType: 'STUDY',
          label: '공부',
          iconUrl: '/images/actions/study.png',
          effects: {
            cash: 0,
            health: 0,
            fatigue: 15,
            stress: 10,
            happiness: 0,
            knowledge: 15,
          },
        },
      ],
    });

    const result = await repository.getAvailableActions(303);

    expect(dataSource.getAvailableActions).toHaveBeenCalledWith(303);
    expect(result.shopping[0]?.actionType).toBe('GROCERY');
    expect(result.activities[0]?.label).toBe('공부');
  });

  it('maps the turn preview response', async () => {
    vi.mocked(dataSource.submitTurnSlots).mockResolvedValue({
      slots: [
        { slotIndex: 0, actionType: 'STUDY', forcedAction: false },
        { slotIndex: 1, actionType: 'REST', forcedAction: false },
        { slotIndex: 2, actionType: 'SIDE_JOB', forcedAction: true },
      ],
      previewCashChange: 430000,
      previewStatChanges: {
        health: 3,
        fatigue: -14,
        stress: -8,
        happiness: 4,
        knowledge: 8,
      },
    });

    const result = await repository.submitTurnSlots(404, [
      { slotIndex: 0, actionType: 'STUDY' },
      { slotIndex: 1, actionType: 'REST' },
      { slotIndex: 2, actionType: 'SIDE_JOB' },
    ]);

    expect(dataSource.submitTurnSlots).toHaveBeenCalledWith(404, {
      slots: [
        { slotIndex: 0, actionType: 'STUDY' },
        { slotIndex: 1, actionType: 'REST' },
        { slotIndex: 2, actionType: 'SIDE_JOB' },
      ],
    });
    expect(result.previewStatChanges.knowledge).toBe(8);
    expect(result.slots[2]?.forcedAction).toBe(true);
  });

  it('maps the turn commit response', async () => {
    vi.mocked(dataSource.commitTurn).mockResolvedValue({
      turnNumber: 12,
      settlementLog: [
        {
          phase: 'ACTION_RESULT',
          description: '턴 행동 결과를 반영한다',
          cashChange: 430000,
          statChanges: {
            health: 3,
            fatigue: -14,
            stress: -8,
            happiness: 4,
            knowledge: 8,
          },
        },
      ],
      updatedAssets: {
        cash: 2820000,
        loan: 0,
        realEstateValue: 0,
        netAssets: 1820000,
      },
      statChanges: {
        health: 3,
        fatigue: -14,
        stress: -8,
        happiness: 4,
        knowledge: 8,
      },
      flags: {
        isBankrupt: false,
        isCleared: false,
        isBurnout: false,
        isForcedResignation: false,
        hasEvent: true,
      },
    });

    const result = await repository.commitTurn(505);

    expect(dataSource.commitTurn).toHaveBeenCalledWith(505);
    expect(result.updatedAssets.netAssets).toBe(1820000);
    expect(result.flags.hasEvent).toBe(true);
  });

  it('throws when the turn state contains an unknown economic cycle phase', async () => {
    vi.mocked(dataSource.getTurn).mockResolvedValue({
      turnNumber: 12,
      currentDate: '2026-01-01',
      month: 1,
      economicCycle: {
        phase: 'UNKNOWN',
        description: '알 수 없는 상태',
      },
    });

    await expect(repository.getTurn(101)).rejects.toThrowError(ResponseMappingError);
  });

  it('throws when the turn state contains an invalid current date', async () => {
    vi.mocked(dataSource.getTurn).mockResolvedValue({
      turnNumber: 12,
      currentDate: 'invalid-date',
      month: 1,
      economicCycle: {
        phase: 'BOOM',
        description: '경기 호황기',
      },
    });

    await expect(repository.getTurn(101)).rejects.toThrowError(ResponseMappingError);
  });

  it('throws when the latest news contains an unknown economic cycle type', async () => {
    vi.mocked(dataSource.getLatestNews).mockResolvedValue({
      turnNumber: 12,
      currentDate: '2026-01-01',
      news: [
        {
          newsId: 'news-1',
          headline: '부동산 시장 과열 경고',
          content: '시장 과열 신호가 확인됐다.',
          sourceName: '영남일보',
          publishedDate: '2026-01-01',
          economicCycleType: 'UNKNOWN',
        },
      ],
    });

    await expect(repository.getLatestNews(202)).rejects.toThrowError(ResponseMappingError);
  });
});
