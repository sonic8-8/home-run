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
    window.localStorage.setItem(
      'game:guide:progress',
      JSON.stringify({
        start: {
          stepIndex: 0,
          completed: false,
          promptSeen: true,
        },
        main: {
          stepIndex: 0,
          completed: false,
          promptSeen: true,
        },
        property: {
          stepIndex: 0,
          completed: false,
          promptSeen: true,
        },
        propertyLoan: {
          stepIndex: 0,
          completed: false,
          promptSeen: true,
        },
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
      { usr: nextState, key: 'turn-schedule-flow-test', idx: 0 },
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

test.describe('turn schedule flow', () => {
  test('selects three actions, previews the turn, and commits the turn', async ({ page }) => {
    const sessionId = 41;
    let currentDate = '2026-04-01';
    let currentTurnNumber = 1;
    let submittedSlots: Record<string, unknown> | null = null;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'MALE',
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
            slotNumber: 1,
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
            runtimeSnapshot: {
              assets: {
                cashBalance: currentTurnNumber === 1 ? 2_300_000 : 12_500_000,
                netWorth: currentTurnNumber === 1 ? 18_700_000 : 20_100_000,
              },
              stats: {
                health: currentTurnNumber === 1 ? 72 : 74,
                fatigue: currentTurnNumber === 1 ? 28 : 26,
                stress: currentTurnNumber === 1 ? 34 : 32,
                happiness: currentTurnNumber === 1 ? 61 : 63,
                knowledge: currentTurnNumber === 1 ? 55 : 60,
              },
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
      submittedSlots = route.request().postDataJSON() as Record<string, unknown>;
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

    await page.goto('/game');
    await waitForRouteHydration(page);

    await expect(page.getByText('현재 상태')).toBeVisible();
    await expect(page.getByText('2,300,000 원')).toBeVisible();
    await expect(page.getByText('18,700,000 원')).toBeVisible();
    await expect(page.getByText('체력')).toBeVisible();
    await expect(page.getByRole('button', { name: '이번 달 활동 진행' })).toBeVisible();
    await page.getByRole('button', { name: '이번 달 활동 진행' }).click();

    await expect(page.getByRole('heading', { name: '4월 활동' })).toBeVisible();

    await page.getByRole('button', { name: /장보기/ }).click();
    await page.getByRole('button', { name: /공부/ }).click();
    await page.getByRole('button', { name: /휴식/ }).click();
    await page.getByRole('button', { name: '미리보기 확인' }).click();

    await expect.poll(() => submittedSlots).not.toBeNull();
    expect(submittedSlots).toEqual({
      slots: [
        { slotIndex: 0, actionType: 'SHOPPING' },
        { slotIndex: 1, actionType: 'STUDY' },
        { slotIndex: 2, actionType: 'REST' },
      ],
    });

    await expect(page.getByText('선택한 슬롯')).toBeVisible();
    await expect(page.getByText('예상 자금 변화')).toBeVisible();
    await expect(page.getByText('장보기')).toBeVisible();
    await expect(page.getByText('공부')).toBeVisible();
    await expect(page.getByText('휴식')).toBeVisible();

    await page.getByRole('button', { name: '턴 진행 확정' }).click();

    await expect(page.getByText('2번째 턴 정산 완료')).toBeVisible();
    await expect(page.getByText('이벤트 발생 예정')).toBeVisible();
    await expect(page.getByText('월급 정산')).toBeVisible();
    await expect(page.getByRole('button', { name: '확인' })).toBeVisible();
  });

  test('keeps the refreshed HUD snapshot on the main screen after confirming a committed turn', async ({ page }) => {
    const sessionId = 42;
    let currentDate = '2026-04-01';
    let currentTurnNumber = 1;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'MALE',
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
            slotNumber: 1,
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
            runtimeSnapshot: {
              assets: {
                cashBalance: currentTurnNumber === 1 ? 2_300_000 : 2_800_000,
                netWorth: currentTurnNumber === 1 ? 18_700_000 : 19_300_000,
              },
              stats: {
                health: currentTurnNumber === 1 ? 72 : 75,
                fatigue: currentTurnNumber === 1 ? 28 : 24,
                stress: currentTurnNumber === 1 ? 34 : 30,
                happiness: currentTurnNumber === 1 ? 61 : 64,
                knowledge: currentTurnNumber === 1 ? 55 : 60,
              },
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
                actionType: 'REST',
                label: '휴식',
                iconUrl: '',
                effects: {
                  cash: 0,
                  health: 3,
                  fatigue: -4,
                  stress: -4,
                  happiness: 3,
                  knowledge: 0,
                },
              },
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
              { slotIndex: 1, actionType: 'REST', forcedAction: false },
              { slotIndex: 2, actionType: 'STUDY', forcedAction: false },
            ],
            previewCashChange: -60000,
            previewStatChanges: {
              health: 3,
              fatigue: -3,
              stress: -3,
              happiness: 3,
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
                cashChange: 500000,
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
              cash: 2_800_000,
              loan: 0,
              realEstateValue: 0,
              netAssets: 19_300_000,
            },
            statChanges: {
              health: 3,
              fatigue: -4,
              stress: -4,
              happiness: 3,
              knowledge: 5,
            },
            flags: {
              isBankrupt: false,
              isCleared: false,
              isBurnout: false,
              isForcedResignation: false,
              hasEvent: false,
            },
          },
        }),
      });
    });

    await page.goto('/game');
    await waitForRouteHydration(page);

    await expect(page.getByText('2,300,000 원')).toBeVisible();
    await page.getByRole('button', { name: '이번 달 활동 진행' }).click();
    await page.getByRole('button', { name: /장보기/ }).click();
    await page.getByRole('button', { name: /휴식/ }).click();
    await page.getByRole('button', { name: /공부/ }).click();
    await page.getByRole('button', { name: '미리보기 확인' }).click();
    await page.getByRole('button', { name: '턴 진행 확정' }).click();

    await expect(page.getByText('2번째 턴 정산 완료')).toBeVisible();
    await page.getByRole('button', { name: '확인' }).click();

    await expect(page.getByText('2,800,000 원')).toBeVisible();
    await expect(page.getByText('19,300,000 원')).toBeVisible();
    await expect(page.getByText('2번째 달')).toBeVisible();
  });
});
