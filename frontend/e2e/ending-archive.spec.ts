import { expect, test, type Page } from '@playwright/test';

function seedAuthenticatedUser(page: Page) {
  return page.addInitScript(() => {
    try {
      window.localStorage.setItem(
        'auth',
        JSON.stringify({
          state: {
            isAuthenticated: true,
            accessToken: 'access-token',
            refreshToken: 'refresh-token',
            nickname: '엔딩테스터',
            accessTokenExpiresAt: Date.now() + 60 * 60 * 1000,
          },
          version: 0,
        }),
      );
    } catch {
      // localStorage is unavailable on bootstrap documents like about:blank.
    }
  });
}

async function mockArchiveApis(page: Page) {
  await page.route('**/api/games/sessions', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          sessions: [
            {
              sessionId: null,
              slotNumber: 1,
              status: 'EMPTY',
            },
            {
              sessionId: 20,
              slotNumber: 2,
              characterName: '하영',
              totalAssets: 310000000,
              createdAt: '2026-01-04T00:00:00',
              status: 'CLEAR',
            },
            {
              sessionId: 30,
              slotNumber: 3,
              characterName: '지훈',
              totalAssets: 87000000,
              createdAt: '2026-03-12T00:00:00',
              status: 'FORECLOSURE',
            },
          ],
        },
      }),
    });
  });

  await page.route('**/api/games/sessions/20/ending', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          characterType: 'FEMALE',
          endingType: 'CLEAR',
          grade: 'S',
          title: '내 집 마련 성공',
          totalAssets: 350000000,
          totalIncome: 220000000,
          totalExpense: 90000000,
          netProfit: 130000000,
          spendingPattern: {
            topCategory: '주거',
            topCategoryRatio: 0.41,
          },
          achievements: [],
          newsHistories: [],
          eventHistories: [],
          housingHistories: [],
          housingSnapshot: {
            currentHousingType: 'OWNED_APT',
            currentPropertyId: 30,
            targetPropertyId: 30,
          },
        },
      }),
    });
  });

  await page.route('**/api/games/sessions/20/logs', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          timeline: [],
        },
      }),
    });
  });
}

test.describe('ending archive', () => {
  test('opens ending archive from the start screen and enters an archived ending', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockArchiveApis(page);

    await page.goto('/game/start');

    await expect(page.getByRole('button', { name: '엔딩 저장소' })).toBeVisible({ timeout: 15_000 });
    await page.getByRole('button', { name: '엔딩 저장소' }).click();

    await expect(page.getByTestId('ending-archive-page')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByTestId('ending-archive-list')).toContainText('하영', { timeout: 15_000 });
    await expect(page.getByTestId('ending-archive-list')).toContainText('지훈', { timeout: 15_000 });

    await page.getByTestId('ending-archive-card-20').click();

    await expect(page.getByTestId('ending-page')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByTestId('ending-scene')).toContainText('내 집 마련 성공', { timeout: 15_000 });
  });
});
