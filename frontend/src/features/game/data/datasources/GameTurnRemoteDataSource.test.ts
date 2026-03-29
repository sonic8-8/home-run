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

  it('uses the news history endpoint', async () => {
    get.mockResolvedValue({
      data: {
        data: {
          newsHistories: [
            {
              turnNumber: 11,
              newsId: 'NEWS-011',
              headline: '채용 한파 심화',
              publishedDate: '2026-01-01',
            },
          ],
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.getNewsHistory(45);

    expect(get).toHaveBeenCalledWith('/games/sessions/45/news/history');
    expect(result.newsHistories[0]?.newsId).toBe('NEWS-011');
  });

  it('uses the pending events endpoint', async () => {
    get.mockResolvedValue({
      data: {
        data: {
          events: [
            {
              eventId: 301,
              type: 'JOB_TRANSFER',
              title: '이직 제안',
              description: '좋은 조건의 이직 제안이 도착했습니다.',
              imageUrl: '/images/events/job-transfer.png',
              choices: [],
              sender: 'OO 기업 인사팀',
              receiver: '김싸피',
              date: '2026-05-01',
              offeredSalary: 42000000,
              currentSalary: 36000000,
            },
          ],
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.getPendingEvents(46);

    expect(get).toHaveBeenCalledWith('/games/sessions/46/events/pending');
    expect(result.events[0]?.type).toBe('JOB_TRANSFER');
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

  it('uses the event resolve endpoint with a choice payload', async () => {
    post.mockResolvedValue({
      data: {
        data: {
          eventId: 301,
          gameEventId: 1201,
          choiceId: 701,
          selectedChoiceCode: 'ACCEPT',
          resultEffects: [],
          resultSummary: '이직 제안을 수락했습니다.',
        },
      },
    });
    const dataSource = new GameTurnRemoteDataSource();

    const result = await dataSource.resolveEvent(88, 301, { choiceId: 701 });

    expect(post).toHaveBeenCalledWith('/games/sessions/88/events/301/resolve', {
      choiceId: 701,
    });
    expect(result.selectedChoiceCode).toBe('ACCEPT');
  });
});
