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

async function mockExternalScripts(page: Page) {
  await page.route('**/openapi/v3/maps.js*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: '',
    });
  });
}

function seedNewGameRouteState(
  page: Page,
  state: Record<string, unknown>,
) {
  return page.addInitScript((state) => {
    window.history.replaceState(
      { usr: state, key: 'game-create-test', idx: 0 },
      '',
      '/game',
    );
  }, state);
}

async function waitForRouteHydration(page: Page) {
  await expect(
    page.getByText('불러오는 중...', { exact: true }),
  ).toHaveCount(0, {
    timeout: 15_000,
  });
}

async function mockCreateSessionFlow(page: Page) {
  let createPayload: Record<string, unknown> | null = null;
  let turnRequestCount = 0;
  let latestNewsRequestCount = 0;

  await page.route(/\/api\/games\/sessions$/, async (route) => {
    if (route.request().method() !== 'POST') {
      await route.fallback();
      return;
    }

    createPayload = route.request().postDataJSON() as Record<string, unknown>;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          sessionId: 31,
          slotNumber: 3,
          sessionStatus: 'IN_PROGRESS',
          currentTurn: 1,
          dataSourceType: 'MY_DATA',
        },
      }),
    });
  });

  await page.route(/\/api\/games\/sessions\/\d+\/turn$/, async (route) => {
    turnRequestCount += 1;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          turnNumber: 1,
          currentDate: '2026-03-27',
          month: 3,
          economicCycle: {
            phase: 'RECOVERY',
            description: '회복 국면',
          },
        },
      }),
    });
  });

  await page.route(/\/api\/games\/sessions\/\d+\/news\/latest$/, async (route) => {
    latestNewsRequestCount += 1;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          turnNumber: 1,
          currentDate: '2026-03-27',
          news: [
            {
              newsId: 'news-1',
              headline: '경제 회복 시작',
              content: '신규 세션 생성 이후 첫 뉴스입니다.',
              sourceName: '홈런경제',
              publishedDate: '2026-03-27',
              economicCycleType: 'RECOVERY',
            },
          ],
        },
      }),
    });
  });

  return {
    getCreatePayload: () => createPayload,
    getTurnRequestCount: () => turnRequestCount,
    getLatestNewsRequestCount: () => latestNewsRequestCount,
  };
}

test.describe('game session create api', () => {
  test('creates a MY_DATA session without jobType in route state', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await seedNewGameRouteState(page, {
      slotNumber: 3,
      characterType: 'FEMALE',
      characterName: '테스터',
      regionCode: '11',
      districtCode: '11680',
      targetPropertyId: 101,
      useMyData: true,
    });
    const {
      getCreatePayload,
      getTurnRequestCount,
      getLatestNewsRequestCount,
    } = await mockCreateSessionFlow(page);

    await page.goto('/game');
    await expect(page).toHaveURL(/\/game$/);
    await waitForRouteHydration(page);
    await expect.poll(getCreatePayload, { timeout: 15_000 }).not.toBeNull();
    await expect.poll(getTurnRequestCount, { timeout: 15_000 }).toBeGreaterThan(0);
    await expect.poll(getLatestNewsRequestCount, { timeout: 15_000 }).toBeGreaterThan(0);
    await expect(page.getByRole('heading', { name: '메뉴' })).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('경제 회복 시작')).toBeVisible({ timeout: 15_000 });

    expect(getCreatePayload()).toEqual({
      slotNumber: 3,
      characterType: 'FEMALE',
      characterName: '테스터',
      regionCode: '11',
      districtCode: '11680',
      targetPropertyId: 101,
      useMyData: true,
    });
  });

  test('creates a PROFILE session with the selected jobType', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await seedNewGameRouteState(page, {
      slotNumber: 2,
      characterType: 'MALE',
      characterName: '홍길동',
      jobType: 'STARTUP',
      regionCode: '26',
      districtCode: '26110',
      targetPropertyId: 202,
      useMyData: false,
    });
    const {
      getCreatePayload,
      getTurnRequestCount,
      getLatestNewsRequestCount,
    } = await mockCreateSessionFlow(page);

    await page.goto('/game');
    await waitForRouteHydration(page);
    await expect.poll(getCreatePayload, { timeout: 15_000 }).not.toBeNull();
    await expect.poll(getTurnRequestCount, { timeout: 15_000 }).toBeGreaterThan(0);
    await expect.poll(getLatestNewsRequestCount, { timeout: 15_000 }).toBeGreaterThan(0);
    await expect(page.getByRole('heading', { name: '메뉴' })).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('경제 회복 시작')).toBeVisible({ timeout: 15_000 });

    expect(getCreatePayload()).toEqual({
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
