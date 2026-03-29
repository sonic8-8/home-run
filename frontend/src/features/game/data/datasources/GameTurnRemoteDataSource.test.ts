import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({
  get: vi.fn(),
}));

const { post } = vi.hoisted(() => ({
  post: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
    post,
  },
}));

import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource';

describe('GameTurnRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
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

  it('uses the turn actions endpoint', async () => {
    get.mockResolvedValue({
      data: {
        data: {
          shopping: [],
          activities: [],
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.getAvailableActions(55);

    expect(get).toHaveBeenCalledWith('/games/sessions/55/turn/actions');
    expect(result).toEqual({
      shopping: [],
      activities: [],
    });
  });

  it('uses the turn slots preview endpoint with slot payload', async () => {
    post.mockResolvedValue({
      data: {
        data: {
          slots: [],
          previewCashChange: 430000,
          previewStatChanges: {
            health: 3,
            fatigue: -14,
            stress: -8,
            happiness: 4,
            knowledge: 8,
          },
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.submitTurnSlots(66, {
      slots: [
        { slotIndex: 0, actionType: 'STUDY' },
        { slotIndex: 1, actionType: 'REST' },
        { slotIndex: 2, actionType: 'SIDE_JOB' },
      ],
    });

    expect(post).toHaveBeenCalledWith('/games/sessions/66/turn/slots', {
      slots: [
        { slotIndex: 0, actionType: 'STUDY' },
        { slotIndex: 1, actionType: 'REST' },
        { slotIndex: 2, actionType: 'SIDE_JOB' },
      ],
    });
    expect(result.previewCashChange).toBe(430000);
  });

  it('uses the turn commit endpoint', async () => {
    post.mockResolvedValue({
      data: {
        data: {
          turnNumber: 12,
          settlementLog: [],
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
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.commitTurn(77);

    expect(post).toHaveBeenCalledWith('/games/sessions/77/turn/commit');
    expect(result.turnNumber).toBe(12);
  });
});
