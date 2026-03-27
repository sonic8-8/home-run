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

async function mockGameMainTurn(page: Page) {
  await page.route('**/api/games/sessions/*/turn', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          turnNumber: 12,
          currentDate: '2026-03-06',
          month: 3,
          economicCycle: {
            phase: 'RECOVERY',
            description: '회복 국면',
          },
        },
      }),
    });
  });
}

async function mockLatestNews(page: Page) {
  await page.route('**/api/games/sessions/*/news/latest', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          turnNumber: 12,
          currentDate: '2026-03-06',
          news: [
            {
              newsId: 'news-1',
              headline: '회복 국면 뉴스',
              content: '경제가 회복세에 접어들었습니다.',
              sourceName: '홈런경제',
              publishedDate: '2026-03-06',
              economicCycleType: 'RECOVERY',
            },
          ],
        },
      }),
    });
  });
}

async function mockCharacterOptions(page: Page) {
  await page.route('**/api/games/characters', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          characters: [
            { characterType: 'FEMALE', thumbnailUrl: '/assets/images/gcharac.png' },
            { characterType: 'MALE', thumbnailUrl: '/assets/images/bcharac.png' },
          ],
        },
      }),
    });
  });
}

test.describe('game save slot selection', () => {
  test('opens the game main page when clicking an existing save slot', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockGameSlots(page);
    await mockGameMainTurn(page);
    await mockLatestNews(page);
    await page.goto('/game/save');

    await page.getByRole('button', { name: '슬롯 1 불러오기' }).click();

    await expect(page).toHaveURL(/\/game$/);
    await expect(page.getByRole('heading', { name: '내 자산' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'USER' })).toBeVisible();
  });

  test('opens the character selection page when clicking an empty slot', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockGameSlots(page);
    await mockCharacterOptions(page);
    await page.goto('/game/save');

    await page.getByRole('button', { name: '슬롯 3 새 게임 시작' }).click();

    await expect(page).toHaveURL(/\/game\/select-character$/);
    await expect(page.getByRole('button', { name: '여자 캐릭터 선택' })).toBeVisible();
    await expect(page.getByRole('button', { name: '남자 캐릭터 선택' })).toBeVisible();
  });
});
