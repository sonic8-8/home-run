import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ResponseMappingError } from '@core/error/AppError';
import hobbyActionIconUrl from '@assets/images/monthly/hobby.png';
import meetFriendActionIconUrl from '@assets/images/monthly/meet_friend-Photoroom.png';
import sideJobActionIconUrl from '@assets/images/monthly/sidejob-Photoroom.png';
import studyActionIconUrl from '@assets/images/monthly/study-Photoroom.png';
import type {
  GameNewsHistoryResponseModel,
  GameTurnResponseModel,
  LatestTurnNewsResponseModel,
  PendingEventsResponseModel,
  ResolveEventResponseModel,
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
      getNewsHistory: vi.fn<(_: number) => Promise<GameNewsHistoryResponseModel>>(),
      getPendingEvents: vi.fn<(_: number) => Promise<PendingEventsResponseModel>>(),
      resolveEvent: vi.fn<
        (_: number, __: number, ___?: { choiceId?: number }) => Promise<ResolveEventResponseModel>
      >(),
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

  it('maps the news history response', async () => {
    vi.mocked(dataSource.getNewsHistory).mockResolvedValue({
      newsHistories: [
        {
          turnNumber: 11,
          newsId: 'NEWS-011',
          headline: '채용 한파 심화',
          publishedDate: '2026-01-01',
        },
      ],
    });

    const result = await repository.getNewsHistory(203);

    expect(dataSource.getNewsHistory).toHaveBeenCalledWith(203);
    expect(result).toEqual([
      {
        turnNumber: 11,
        newsId: 'NEWS-011',
        headline: '채용 한파 심화',
        publishedDate: new Date('2026-01-01T00:00:00'),
      },
    ]);
  });

  it('maps the pending events response', async () => {
    vi.mocked(dataSource.getPendingEvents).mockResolvedValue({
      events: [
        {
          eventId: 301,
          type: 'JOB_TRANSFER',
          title: '이직 제안',
          description: '좋은 조건의 이직 제안이 도착했습니다.',
          imageUrl: '/images/events/job-transfer.png',
          choices: [
            {
              choiceId: 701,
              choiceCode: 'ACCEPT',
              choiceName: '승인하기',
              description: '이직을 수락합니다.',
            },
          ],
          sender: 'OO 기업 인사팀',
          receiver: '김싸피',
          date: '2026-05-01',
          offeredSalary: 42000000,
          currentSalary: 36000000,
        },
      ],
    });

    const result = await repository.getPendingEvents(204);

    expect(dataSource.getPendingEvents).toHaveBeenCalledWith(204);
    expect(result[0]).toEqual({
      eventId: 301,
      type: 'JOB_TRANSFER',
      title: '이직 제안',
      description: '좋은 조건의 이직 제안이 도착했습니다.',
      imageUrl: '/images/events/job-transfer.png',
      choices: [
        {
          choiceId: 701,
          choiceCode: 'ACCEPT',
          choiceName: '승인하기',
          description: '이직을 수락합니다.',
        },
      ],
      sender: 'OO 기업 인사팀',
      receiver: '김싸피',
      date: new Date('2026-05-01T00:00:00'),
      offeredSalary: 42000000,
      currentSalary: 36000000,
    });
  });

  it('maps the resolve event response', async () => {
    vi.mocked(dataSource.resolveEvent).mockResolvedValue({
      eventId: 301,
      gameEventId: 1201,
      choiceId: 701,
      selectedChoiceCode: 'ACCEPT',
      resultEffects: [
        {
          effectOrder: 1,
          applicationTimingType: 'IMMEDIATE',
          targetTableName: 'game_career',
          targetColumnName: 'salary',
          operationType: 'ADD',
          baseNumberValue: 6000000,
          minNumberValue: null,
          maxNumberValue: null,
          baseTextValue: null,
          durationTurns: null,
          note: '연봉이 올랐습니다.',
        },
      ],
      resultSummary: '이직 제안을 수락했습니다.',
    });

    const result = await repository.resolveEvent(205, 301, 701);

    expect(dataSource.resolveEvent).toHaveBeenCalledWith(205, 301, { choiceId: 701 });
    expect(result.resultSummary).toBe('이직 제안을 수락했습니다.');
    expect(result.resultEffects[0]?.note).toBe('연봉이 올랐습니다.');
  });

  it('maps known action icons to bundled monthly images and preserves unknown icon urls', async () => {
    vi.mocked(dataSource.getAvailableActions).mockResolvedValue({
      shopping: [
        {
          actionType: 'HOBBY',
          label: '취미',
          iconUrl: '/images/actions/hobby.png',
          effects: {
            cash: -100000,
            health: 0,
            fatigue: -2,
            stress: -6,
            happiness: 6,
            knowledge: 0,
          },
        },
        {
          actionType: 'MEET_FRIEND',
          label: '친구 만나기',
          iconUrl: '/images/actions/meet-friend.png',
          effects: {
            cash: -150000,
            health: -3,
            fatigue: 4,
            stress: -5,
            happiness: 10,
            knowledge: 2,
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
        {
          actionType: 'SIDE_JOB',
          label: '부업',
          iconUrl: '/images/actions/side-job.png',
          effects: {
            cash: 0,
            health: -3,
            fatigue: 10,
            stress: 8,
            happiness: 0,
            knowledge: 0,
          },
        },
        {
          actionType: 'CUSTOM',
          label: '커스텀',
          iconUrl: '/images/actions/custom.png',
          effects: {
            cash: 1,
            health: 2,
            fatigue: 3,
            stress: 4,
            happiness: 5,
            knowledge: 6,
          },
        },
      ],
    });

    const result = await repository.getAvailableActions(303);

    expect(dataSource.getAvailableActions).toHaveBeenCalledWith(303);
    expect(result.shopping[0]?.actionType).toBe('HOBBY');
    expect(result.shopping[0]?.iconUrl).toBe(hobbyActionIconUrl);
    expect(result.shopping[1]?.iconUrl).toBe(meetFriendActionIconUrl);
    expect(result.activities[0]?.label).toBe('공부');
    expect(result.activities[0]?.iconUrl).toBe(studyActionIconUrl);
    expect(result.activities[1]?.iconUrl).toBe(sideJobActionIconUrl);
    expect(result.activities[2]?.iconUrl).toBe('/images/actions/custom.png');
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

  it('throws when the pending event contains an unsupported presentation type', async () => {
    vi.mocked(dataSource.getPendingEvents).mockResolvedValue({
      events: [
        {
          eventId: 301,
          type: 'UNKNOWN',
          title: '알 수 없는 이벤트',
          description: '지원하지 않는 이벤트입니다.',
          imageUrl: null,
          choices: [],
          sender: null,
          receiver: null,
          date: null,
          offeredSalary: null,
          currentSalary: null,
        },
      ],
    });

    await expect(repository.getPendingEvents(206)).rejects.toThrowError(ResponseMappingError);
  });
});
