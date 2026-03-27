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

test.describe('game start navigation', () => {
  test('navigates to the save slot page when clicking continue', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '이어하기' }).click();

    await expect(page).toHaveURL(/\/game\/save$/);
    await expect(page.getByText('SAVE')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 1 불러오기' })).toBeVisible();
  });

  test('navigates to the save slot page when clicking new game', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await page.goto('/game/start');

    await page.getByRole('button', { name: '새로하기' }).click();

    await expect(page).toHaveURL(/\/game\/save$/);
    await expect(page.getByText('SAVE')).toBeVisible();
    await expect(page.getByRole('button', { name: '슬롯 3 비어 있음' })).toBeVisible();
  });
});
