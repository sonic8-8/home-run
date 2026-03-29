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
            {
              characterType: 'FEMALE',
              thumbnailUrl: '/images/characters/female.png',
            },
            {
              characterType: 'MALE',
              thumbnailUrl: '/images/characters/male.png',
            },
          ],
        },
      }),
    });
  });
}

test.describe('character thumbnail fallback', () => {
  test('falls back to local assets when the api thumbnail image fails to load', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockCharacterOptions(page);

    await page.goto('/game/select-character');

    const femaleImage = page.locator('img[alt="FEMALE"]').first();
    const maleImage = page.locator('img[alt="MALE"]').first();

    await expect(femaleImage).toHaveAttribute('src', /assets\/images\/gcharac\.png$/, {
      timeout: 15_000,
    });
    await expect(maleImage).toHaveAttribute('src', /assets\/images\/bcharac\.png$/, {
      timeout: 15_000,
    });
  });
});
