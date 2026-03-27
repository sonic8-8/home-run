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

function mockUserMeWithoutAssetLink(page: Page) {
  return page.route('**/api/users/me', async (route) => {
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          userId: 1,
          email: 'tester@example.com',
          name: '테스터',
          isAssetLinked: false,
          totalAssetAmount: null,
        },
      }),
    });
  });
}

test.describe('auth routing', () => {
  test('redirects unauthenticated users from a private route to /login', async ({ page }) => {
    await page.goto('/loan');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();
  });

  test('allows authenticated users to open the protected game start page', async ({ page }) => {
    await seedAuthenticatedUser(page);

    await page.goto('/game/start');

    await expect(page).toHaveURL(/\/game\/start$/);
    await expect(page.getByRole('button', { name: '이어하기' })).toBeVisible();
    await expect(page.getByRole('button', { name: '새로하기' })).toBeVisible();
  });

  test('redirects authenticated users away from /login to the home flow', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockUserMeWithoutAssetLink(page);

    await page.goto('/login');

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByText('마이데이터 연동이 필요합니다')).toBeVisible();
    await expect(page.getByRole('button', { name: '마이데이터 연동하기' })).toBeVisible();
  });
});
