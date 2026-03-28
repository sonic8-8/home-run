import { expect, test, type Page } from '@playwright/test';

function seedAuthenticatedUser(page: Page) {
  return page.addInitScript(() => {
    window.localStorage.setItem(
      'auth',
      JSON.stringify({
        state: {
          isAuthenticated: true,
          accessToken: 'access-token',
          refreshToken: 'refresh-token',
          nickname: '테스터',
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

type CreatePayloadWindow = Window & {
  __testCreatePayload: Record<string, unknown> | null;
};

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

async function mockCreateSessionFlow(page: Page) {
  await page.addInitScript(() => {
    const testWindow = window as CreatePayloadWindow;
    testWindow.__testCreatePayload = null;

    const originalFetch = window.fetch.bind(window);

    window.fetch = async (input, init) => {
      const request = input instanceof Request ? input : null;
      const url = typeof input === 'string' ? input : request?.url ?? String(input);
      const method = init?.method ?? request?.method ?? 'GET';

      if (url.endsWith('/api/games/sessions') && method === 'POST') {
        testWindow.__testCreatePayload = JSON.parse(String(init?.body ?? '{}'));

        return new Response(
          JSON.stringify({
            status: 201,
            message: 'OK',
            data: {
              sessionId: 31,
              slotNumber: 3,
              sessionStatus: 'IN_PROGRESS',
              currentTurn: 1,
              dataSourceType: 'MY_DATA',
            },
          }),
          {
            status: 201,
            headers: {
              'Content-Type': 'application/json',
            },
          },
        );
      }

      if (/\/api\/games\/sessions\/\d+\/turn$/.test(url)) {
        return new Response(
          JSON.stringify({
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
          {
            status: 200,
            headers: {
              'Content-Type': 'application/json',
            },
          },
        );
      }

      if (/\/api\/games\/sessions\/\d+\/news\/latest$/.test(url)) {
        return new Response(
          JSON.stringify({
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
          {
            status: 200,
            headers: {
              'Content-Type': 'application/json',
            },
          },
        );
      }

      return originalFetch(input, init);
    };
  });
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
    await mockCreateSessionFlow(page);

    await page.goto('/game');

    await page.waitForFunction(() => {
      const testWindow = window as CreatePayloadWindow;
      return testWindow.__testCreatePayload !== null;
    });
    await expect(page).toHaveURL(/\/game$/);
    await expect(page.getByText('게임 정보를 불러오는 중입니다.')).toBeVisible();

    const createPayload = await page.evaluate(() => {
      const testWindow = window as CreatePayloadWindow;
      return testWindow.__testCreatePayload;
    });

    expect(createPayload).toEqual({
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
    await mockCreateSessionFlow(page);

    await page.goto('/game');

    await page.waitForFunction(() => {
      const testWindow = window as CreatePayloadWindow;
      return testWindow.__testCreatePayload !== null;
    });

    const createPayload = await page.evaluate(() => {
      const testWindow = window as CreatePayloadWindow;
      return testWindow.__testCreatePayload;
    });

    expect(createPayload).toEqual({
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
