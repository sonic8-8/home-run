import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({
  get: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
  },
}));

import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource';

describe('GameTurnRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
  });

  it('uses the game turn state endpoint', async () => {
    get.mockResolvedValue({
      data: {
        data: {
          turnNumber: 12,
          currentDate: '2026-01-01',
          month: 1,
          economicCycle: {
            phase: 'BOOM',
            description: '경기 호황기',
          },
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.getTurn(33);

    expect(get).toHaveBeenCalledWith('/games/sessions/33/turn');
    expect(result.turnNumber).toBe(12);
  });

  it('uses the latest turn news endpoint', async () => {
    get.mockResolvedValue({
      data: {
        data: {
          turnNumber: 12,
          currentDate: '2026-01-01',
          news: [],
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.getLatestNews(44);

    expect(get).toHaveBeenCalledWith('/games/sessions/44/news/latest');
    expect(result.news).toEqual([]);
  });
});
