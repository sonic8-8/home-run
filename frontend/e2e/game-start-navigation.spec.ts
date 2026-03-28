import { test, expect, type Page } from '@playwright/test';

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

async function mockGameSlots(page: Page) {
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
              sessionId: 1,
              slotNumber: 1,
              characterName: '김싸피',
              jobType: 'LARGE_BIZ',
              totalAssets: 1000000,
              createdAt: '2026-03-06T00:00:00',
              currentTurn: 12,
              status: 'IN_PROGRESS',
            },
            {
              sessionId: 2,
              slotNumber: 2,
              characterName: '박홈런',
              jobType: 'FREELANCER',
              totalAssets: 500000,
              createdAt: '2026-03-06T00:00:00',
              currentTurn: 3,
              status: 'IN_PROGRESS',
            },
            {
              sessionId: null,
              slotNumber: 3,
              status: 'EMPTY',
            },
          ],
        },
      }),
    });
  });
}

async function mockJobTypes(page: Page) {
  await page.route('**/api/games/job-types', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: [
          {
            jobType: 'STARTUP',
            label: '스타트업 직장인',
            stats: {
              salary: 60,
              health: 55,
              stability: 40,
              growthSpeed: 80,
              difficulty: 65,
            },
          },
        ],
      }),
    });
  });
}

function seedSelectStartMethodRouteState(page: Page) {
  return page.addInitScript((state) => {
    window.history.replaceState(
      { usr: state, key: 'game-start-method-test', idx: 0 },
      '',
      '/game/select-start-method',
    );
  }, {
    slotNumber: 3,
    characterType: 'FEMALE',
    characterName: '테스터',
  });
}

test.describe('game start navigation', () => {
  test('navigates to the save slot page when clicking continue', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await mockGameSlots(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '이어하기' }).click();

    await expect(page).toHaveURL(/\/game\/save$/);
    await expect(page.getByText('SAVE')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 1 불러오기' })).toBeVisible();
  });

  test('navigates to the save slot page when clicking new game', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await mockGameSlots(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '새로하기' }).click();

    await expect(page).toHaveURL(/\/game\/save$/);
    await expect(page.getByText('SAVE')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 3 새 게임 시작' })).toBeVisible();
  });

  test('skips job selection when starting with my data', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await seedSelectStartMethodRouteState(page);

    await page.goto('/game/select-start-method');

    await page.getByRole('button', { name: '자산 연결해서 시작하기' }).click();
    await page.getByRole('button', { name: 'NEXT >' }).click();

    await expect(page).toHaveURL(/\/property$/);
  });

  test('keeps job selection when starting with profile', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await mockJobTypes(page);
    await seedSelectStartMethodRouteState(page);

    await page.goto('/game/select-start-method');

    await page.getByRole('button', { name: '직업 선택하기' }).click();
    await page.getByRole('button', { name: 'NEXT >' }).click();

    await expect(page).toHaveURL(/\/game\/select-job$/);
  });
});
