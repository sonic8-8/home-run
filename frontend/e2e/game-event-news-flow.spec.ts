import { expect, test, type Page } from '@playwright/test';

async function seedAuthenticatedUser(page: Page) {
  await page.route('**/auth/refresh', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          accessToken: 'refreshed-access-token',
          accessTokenExpiresIn: 1800,
        },
      }),
    });
  });

  await page.addInitScript(() => {
    window.localStorage.setItem(
      'auth',
      JSON.stringify({
        state: {
          isAuthenticated: true,
          accessToken: 'access-token',
          refreshToken: 'refresh-token',
          nickname: '테스터',
          accessTokenExpiresAt: Date.now() + 3_600_000,
        },
        version: 0,
      }),
    );
  });
}

function seedRouteState(
  page: Page,
  path: string,
  state: Record<string, unknown>,
) {
  return page.addInitScript(({ nextPath, nextState }) => {
    window.history.replaceState(
      { usr: nextState, key: 'game-event-news-flow-test', idx: 0 },
      '',
      nextPath,
    );
  }, { nextPath: path, nextState: state });
}

function seedNewsSeenDate(page: Page, sessionId: number, date: string) {
  return page.addInitScript(({ nextSessionId, nextDate }) => {
    window.sessionStorage.setItem(`news_seen_date_${nextSessionId}`, nextDate);
  }, { nextSessionId: sessionId, nextDate: date });
}

async function waitForRouteHydration(page: Page) {
  await expect(
    page.getByText('불러오는 중...', { exact: true }),
  ).toHaveCount(0, {
    timeout: 15_000,
  });
}

test.describe('game event and news flow', () => {
  test('resolves a pending event after turn commit and shows news history', async ({ page }) => {
    const sessionId = 52;
    let currentDate = '2026-04-01';
    let currentTurnNumber = 1;
    let resolvedChoicePayload: Record<string, unknown> | null = null;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'FEMALE',
    });

    await page.route(`**/api/games/sessions/${sessionId}`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            sessionId,
            slotNumber: 2,
            currentTurn: currentTurnNumber,
            currentDate,
            status: 'IN_PROGRESS',
            createdAt: '2026-04-01',
            endedAt: null,
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/turn`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            turnNumber: currentTurnNumber,
            currentDate,
            month: currentTurnNumber === 1 ? 4 : 5,
            economicCycle: {
              phase: 'RECOVERY',
              description: '회복 국면',
            },
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/turn/actions`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            shopping: [
              {
                actionType: 'SHOPPING',
                label: '장보기',
                iconUrl: '',
                effects: {
                  cash: -50000,
                  health: 0,
                  fatigue: 0,
                  stress: -1,
                  happiness: 1,
                  knowledge: 0,
                },
              },
            ],
            activities: [
              {
                actionType: 'STUDY',
                label: '공부',
                iconUrl: '',
                effects: {
                  cash: -10000,
                  health: 0,
                  fatigue: 1,
                  stress: 1,
                  happiness: 0,
                  knowledge: 5,
                },
              },
              {
                actionType: 'REST',
                label: '휴식',
                iconUrl: '',
                effects: {
                  cash: 0,
                  health: 2,
                  fatigue: -3,
                  stress: -2,
                  happiness: 1,
                  knowledge: 0,
                },
              },
            ],
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/turn/slots`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            slots: [
              { slotIndex: 0, actionType: 'SHOPPING', forcedAction: false },
              { slotIndex: 1, actionType: 'STUDY', forcedAction: false },
              { slotIndex: 2, actionType: 'REST', forcedAction: false },
            ],
            previewCashChange: -60000,
            previewStatChanges: {
              health: 2,
              fatigue: -2,
              stress: -2,
              happiness: 2,
              knowledge: 5,
            },
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/turn/commit`, async (route) => {
      currentTurnNumber = 2;
      currentDate = '2026-05-01';
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            turnNumber: 2,
            settlementLog: [
              {
                phase: 'INCOME',
                description: '월급 정산',
                cashChange: 2500000,
                statChanges: {
                  health: 0,
                  fatigue: 0,
                  stress: 0,
                  happiness: 0,
                  knowledge: 0,
                },
              },
            ],
            updatedAssets: {
              cash: 12500000,
              loan: 0,
              realEstateValue: 0,
              netAssets: 12500000,
            },
            statChanges: {
              health: 2,
              fatigue: -2,
              stress: -2,
              happiness: 2,
              knowledge: 5,
            },
            flags: {
              isBankrupt: false,
              isCleared: false,
              isBurnout: false,
              isForcedResignation: false,
              hasEvent: true,
            },
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/events/pending`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            events: [
              {
                eventId: 301,
                type: 'JOB_TRANSFER',
                title: '열심히 일한 당신! 이직하시겠습니까?',
                description: '더 좋은 조건의 제안이 도착했습니다.',
                imageUrl: '/images/events/job-transfer.png',
                choices: [
                  {
                    choiceId: 701,
                    choiceCode: 'ACCEPT',
                    choiceName: '승인하기',
                    description: '더 좋은 조건으로 이직합니다.',
                  },
                  {
                    choiceId: 702,
                    choiceCode: 'REJECT',
                    choiceName: '거절하기',
                    description: '현재 직장에 남습니다.',
                  },
                ],
                sender: 'OO 기업 인사팀',
                receiver: '김싸피',
                date: '2026-05-01',
                offeredSalary: 42000000,
                currentSalary: 36000000,
              },
            ],
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/events/301/resolve`, async (route) => {
      resolvedChoicePayload = route.request().postDataJSON() as Record<string, unknown>;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
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
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/news/latest`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            turnNumber: 2,
            currentDate: '2026-05-01',
            news: [
              {
                newsId: 'NEWS-020',
                headline: '기준 금리 동결',
                content: '기준 금리가 동결되며 시장 관망세가 이어졌습니다.',
                sourceName: '홈런경제',
                publishedDate: '2026-05-01',
                economicCycleType: 'RECOVERY_TO_RECOVERY',
              },
            ],
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/news/history`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            newsHistories: [
              {
                turnNumber: 1,
                newsId: 'NEWS-010',
                headline: '채용 한파 심화',
                publishedDate: '2026-04-01',
              },
              {
                turnNumber: 0,
                newsId: 'NEWS-009',
                headline: '소비 심리 회복세',
                publishedDate: '2026-03-01',
              },
            ],
          },
        }),
      });
    });

    await page.goto('/game');
    await waitForRouteHydration(page);

    await page.getByRole('button', { name: '이번 달 활동 진행' }).click();
    await expect(page.getByRole('heading', { name: '4월 활동' })).toBeVisible();

    await page.getByRole('button', { name: /장보기/ }).click();
    await page.getByRole('button', { name: /공부/ }).click();
    await page.getByRole('button', { name: /휴식/ }).click();
    await page.getByRole('button', { name: '미리보기 확인' }).click();
    await page.getByRole('button', { name: '턴 진행 확정' }).click();

    await expect(page.getByText('2번째 턴 정산 완료')).toBeVisible();
    await expect(page.getByRole('button', { name: '이벤트 확인' })).toBeVisible();
    await page.getByRole('button', { name: '이벤트 확인' }).click();

    await expect(page.getByText('새로 도착한 메일')).toBeVisible();
    await page.getByRole('button', { name: '승인하기' }).click();

    await expect.poll(() => resolvedChoicePayload).not.toBeNull();
    expect(resolvedChoicePayload).toEqual({ choiceId: 701 });

    await expect(page.getByText('이직 제안을 수락했습니다.')).toBeVisible();
    await expect(page.getByText('연봉이 올랐습니다.')).toBeVisible();
    await page.getByRole('button', { name: '확인' }).last().click();

    await expect(page.getByText('이직 제안을 수락했습니다.')).toHaveCount(0);
    await expect(page.getByRole('button', { name: '지난 뉴스 보기' })).toBeVisible();
    await page.getByRole('button', { name: '지난 뉴스 보기' }).click();

    await expect(page).toHaveURL(new RegExp(`/game/${sessionId}/news$`));
    await expect(page.getByText('홈런 경제 신문')).toBeVisible();
    await expect(page.getByText('기준 금리 동결')).toBeVisible();
    await expect(page.getByText('지난 턴 헤드라인')).toBeVisible();
    await expect(page.getByText('채용 한파 심화')).toBeVisible();
  });
});
