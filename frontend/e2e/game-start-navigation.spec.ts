import { test, expect, type Page } from '@playwright/test';

import {
  mockExternalScripts,
  seedAuthenticatedUser,
  seedRouteState,
} from './support/runtimeFixtures';

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
  return seedRouteState(page, {
    pathname: '/game/select-start-method',
    key: 'game-start-method-test',
    state: {
      slotNumber: 3,
      characterType: 'FEMALE',
      characterName: '테스터',
    },
  });
}

test.describe('game start navigation', () => {
  test('navigates to the save slot page when clicking continue', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await mockGameSlots(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '이어하기' }).click();

    await expect(page).toHaveURL(/\/game\/save\?mode=continue$/);
    await expect(page.getByText('이어할 세션 선택')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 1 불러오기' })).toBeVisible();
  });

  test('navigates to the save slot page when clicking new game', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await mockGameSlots(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '새로하기' }).click();

    await expect(page).toHaveURL(/\/game\/save\?mode=new$/);
    await expect(page.getByText('새로 시작할 슬롯 선택')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 3 새 게임 시작' })).toBeVisible();
  });

  test('opens the guide overlay from the game start screen', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '게임 가이드' }).click();

    await expect(page).toHaveURL(/\/game\/start$/);
    await expect(page.getByRole('heading', { name: '새로하기부터 시작' })).toBeVisible();
    await expect(page.getByText('1 / 5')).toBeVisible();
    await page.getByRole('button', { name: '닫기', exact: true }).click();
    await expect(page).toHaveURL(/\/game\/start$/);
    await expect(page.getByRole('heading', { name: '새로하기부터 시작' })).toHaveCount(0);
  });

  test('skips job selection when starting with my data', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockExternalScripts(page);
    await seedSelectStartMethodRouteState(page);

    await page.goto('/game/select-start-method');

    await page.getByRole('button', { name: '자산 연결해서 시작하기' }).click();
    await page.getByRole('button', { name: 'NEXT >' }).click();

    await expect(page).toHaveURL(/\/property\/new-game$/);
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
