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
    try {
      window.localStorage.setItem(
        'auth',
        JSON.stringify({
          state: {
            isAuthenticated: true,
            accessToken: 'access-token',
            refreshToken: 'refresh-token',
            nickname: '홈테스터',
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

async function mockHomeApis(page: Page) {
  await page.route('**/api/users/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          id: 1,
          email: 'test@example.com',
          nickname: '홈테스터',
          isAssetLinked: true,
        },
      }),
    });
  });

  await page.route('**/api/home/dashboard', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          totalAssets: 220000000,
          monthlyIncome: 3500000,
          monthlyExpense: 1800000,
          incomeChangeFromLastMonth: 0.1,
          expenseChangeFromLastMonth: -0.02,
          nextPaydayDays: 3,
          mainAccountBalance: 5100000,
          seedmoneyBalance: 820000,
        },
      }),
    });
  });

  await page.route('**/api/home/spending', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          month: '2026-03',
          totalExpense: 1800000,
          categories: [],
        },
      }),
    });
  });

  await page.route('**/api/seedmoney/account', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          bankName: '홈런은행',
          accountNumber: '123-456',
          balance: 820000,
        },
      }),
    });
  });

  await page.route('**/api/pass/products', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          products: [],
        },
      }),
    });
  });

  await page.route('**/api/pass/subscriptions', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          subscriptions: [
            {
              subscriptionId: 7,
              passId: 2,
              name: '커피 줄이기 PASS',
              amountPerSave: 5000,
              totalSaved: 20000,
              weeklyHistory: [true, false, true, true, false, false, true],
            },
          ],
        },
      }),
    });
  });

  await page.route('**/api/pass/history**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          content: [
            {
              historyId: 1,
              passName: '커피 줄이기 PASS',
              amount: 5000,
              savedAt: '2026-03-29T00:00:00',
            },
            {
              historyId: 2,
              passName: '야식 줄이기 PASS',
              amount: 12000,
              savedAt: '2026-03-27T00:00:00',
            },
          ],
          page: 0,
          size: 10,
          totalElements: 2,
          totalPages: 1,
        },
      }),
    });
  });

  await page.route('**/api/cards', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: { cards: [] },
      }),
    });
  });

  await page.route('**/api/cards/recommendations', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: { recommendations: [] },
      }),
    });
  });

  await page.route('**/api/home/loan-recommendations', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          cssScore: 780,
          cssGrade: 2,
          cssGradeLabel: '우수',
          estimatedMinRate: 3.2,
          creditLoans: [],
          jeonseLoans: [],
          mortgageLoans: [],
        },
      }),
    });
  });

  await page.route('**/api/credit/score', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          score: 780,
          grade: 2,
          gradeLabel: '우수',
          paymentHistory: 95,
          amountsOwed: 80,
          creditLength: 70,
          creditMix: 75,
          newCredit: 65,
          ratingName: '좋음',
          totalAsset: 220000000,
          totalDebt: 15000000,
          netAsset: 205000000,
        },
      }),
    });
  });
}

test.describe('home pass history', () => {
  test('renders recent pass saving history on the home page', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockHomeApis(page);

    await page.goto('/');

    await expect(page.getByTestId('pass-history-list')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('최근 PASS 저축 기록')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('커피 줄이기 PASS')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('야식 줄이기 PASS')).toBeVisible({ timeout: 15_000 });
  });
});
