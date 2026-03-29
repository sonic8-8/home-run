import { test, expect } from '@playwright/test';

test.describe('sign-up flow', () => {
  test('submits the backend-required fields and returns to the login view', async ({ page }) => {
    await page.route('**/api/auth/signup', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: null,
        }),
      });
    });

    await page.goto('/login');

    await page.getByRole('button', { name: '이메일로 회원가입하기' }).click();
    await expect(page.getByRole('button', { name: '회원가입' })).toBeVisible();

    await page.getByPlaceholder('김싸피').fill('테스터');
    await page.getByPlaceholder('example@email.com').fill('tester@example.com');

    const passwordInputs = page.locator('input[placeholder="Password"]');
    await passwordInputs.nth(0).fill('Password123!');
    await passwordInputs.nth(1).fill('Password123!');
    await page.getByRole('checkbox').check();

    const signUpRequest = page.waitForRequest((request) => {
      return request.url().includes('/api/auth/signup') && request.method() === 'POST';
    });

    await page.getByRole('button', { name: '회원가입' }).click();

    const request = await signUpRequest;
    expect(JSON.parse(request.postData() ?? '{}')).toEqual({
      name: '테스터',
      email: 'tester@example.com',
      password: 'Password123!',
      passwordConfirm: 'Password123!',
      termsAgreed: true,
    });

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('button', { name: '로그인' })).toBeVisible();
    await expect(page.getByRole('button', { name: '이메일로 회원가입하기' })).toBeVisible();
  });
});
