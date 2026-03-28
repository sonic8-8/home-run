import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ResponseMappingError } from '@core/error/AppError';
import type {
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
} from '@features/game/data/models/GameTurnModel';
import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource';
import { GameTurnRepositoryImpl } from '@features/game/data/repositories/GameTurnRepositoryImpl';

describe('GameTurnRepositoryImpl', () => {
  let repository: GameTurnRepositoryImpl;
  let dataSource: GameTurnRemoteDataSource;

  beforeEach(() => {
    dataSource = {
      getTurn: vi.fn<(_: number) => Promise<GameTurnResponseModel>>(),
      getLatestNews: vi.fn<(_: number) => Promise<LatestTurnNewsResponseModel>>(),
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
