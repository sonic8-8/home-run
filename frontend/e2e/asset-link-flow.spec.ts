import { test, expect, type Page } from '@playwright/test';

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

interface AssetLinkMockData {
  totalAssetAmount: number;
  netAssetAmount: number;
  totalDebtAmount: number;
  monthlyIncome: number;
  monthlyExpense: number;
  mainAccountBalance: number;
  seedmoneyBalance: number;
}

const defaultAssetLinkMockData: AssetLinkMockData = {
  totalAssetAmount: 6200000,
  netAssetAmount: 5200000,
  totalDebtAmount: 1000000,
  monthlyIncome: 3000000,
  monthlyExpense: 1000000,
  mainAccountBalance: 5000000,
  seedmoneyBalance: 1200000,
};

async function waitForAssetLinkPageReady(page: Page) {
  const userMeResponse = page.waitForResponse((response) => {
    return response.url().includes('/api/users/me') && response.request().method() === 'GET';
  });

  await page.goto('/login');

  await expect(page).toHaveURL(/\/$/);
  await userMeResponse;
  await expect(page.getByPlaceholder('예: 5000000')).toBeVisible();
}

async function mockAssetLinkFlow(
  page: Page,
  mockData: AssetLinkMockData = defaultAssetLinkMockData,
) {
  let isAssetLinked = false;

  await page.route('**/api/users/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          userId: 1,
          email: 'tester@example.com',
          name: '테스터',
          isAssetLinked,
          totalAssetAmount: isAssetLinked ? mockData.totalAssetAmount : null,
          netAssetAmount: isAssetLinked ? mockData.netAssetAmount : null,
        },
      }),
    });
  });

  await page.route('**/api/users/me/asset-link', async (route) => {
    isAssetLinked = true;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          isAssetLinked: true,
          mainAccountCreated: true,
          seedmoneyAccountCreated: true,
          summaryInitialized: true,
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
          totalAssets: mockData.totalAssetAmount,
          monthlyIncome: mockData.monthlyIncome,
          monthlyExpense: mockData.monthlyExpense,
          incomeChangeFromLastMonth: 100000,
          expenseChangeFromLastMonth: -50000,
          nextPaydayDays: 5,
          mainAccountBalance: mockData.mainAccountBalance,
          seedmoneyBalance: mockData.seedmoneyBalance,
        },
      }),
    });
  });

  await page.route('**/api/home/dashboard/subscribe', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'text/event-stream',
      body: [
        'event: dashboard-update',
        `data: ${JSON.stringify({
          totalAssets: mockData.totalAssetAmount,
          monthlyIncome: mockData.monthlyIncome,
          monthlyExpense: mockData.monthlyExpense,
          incomeChangeFromLastMonth: 100000,
          expenseChangeFromLastMonth: -50000,
          nextPaydayDays: 5,
          mainAccountBalance: mockData.mainAccountBalance,
          seedmoneyBalance: mockData.seedmoneyBalance,
        })}`,
        '',
      ].join('\n'),
    });
  });

  await page.route('**/api/home/spending*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          month: '2026-03',
          totalExpense: 450000,
          categories: [
            {
              category: 'LIVING',
              categoryName: '생활/식비',
              amount: 450000,
              ratio: 100,
            },
          ],
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
          accountNumber: '123-456-7890',
          balance: 1200000,
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
          subscriptions: [],
        },
      }),
    });
  });

  await page.route('**/api/pass/history?page=0&size=10', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
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
        data: {
          cards: [],
        },
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
        data: {
          recommendations: [],
        },
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
          cssScore: 820,
          cssGrade: 2,
          cssGradeLabel: '우수',
          estimatedMinRate: 3.1,
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
          score: 820,
          grade: 2,
          gradeLabel: '우수',
          paymentHistory: 320,
          amountsOwed: 250,
          creditLength: 110,
          creditMix: 80,
          newCredit: 60,
          ratingName: '좋음',
          totalAsset: mockData.totalAssetAmount,
          totalDebt: mockData.totalDebtAmount,
          netAsset: mockData.netAssetAmount,
        },
      }),
    });
  });
}

test.describe('asset link flow', () => {
  test('submits the three-step asset link form and returns to the linked home', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockAssetLinkFlow(page);

    await waitForAssetLinkPageReady(page);

    await page.getByPlaceholder('예: 5000000').fill('5000000');
    await page.getByPlaceholder('1 ~ 28').fill('25');
    await page.getByPlaceholder('예: 3000000').fill('3000000');
    await page.getByPlaceholder('예: 1000000').fill('1000000');
    await page.getByRole('combobox').selectOption('LARGE_BIZ');
    await page.getByRole('button', { name: '다음' }).click();

    await expect(page.getByText('보유 금융 자산 정보를 입력해 주세요')).toBeVisible();
    await page.getByRole('button', { name: '+ 예금 추가' }).click();
    await page.getByPlaceholder('이름 (예: 국민은행 적금)').fill('국민은행 적금');
    await page.getByPlaceholder('잔액 (원)').fill('2000000');
    await page.getByRole('button', { name: '+ 대출 추가' }).click();
    await page.getByPlaceholder('이름 (예: 신한 전세대출)').fill('전세대출');
    await page.locator('input[placeholder="잔액 (원)"]').nth(1).fill('10000000');
    await page.getByRole('button', { name: '+ 기타 소득 추가' }).click();
    await page.getByPlaceholder('이름 (예: 유튜브 수익)').fill('유튜브');
    await page.getByPlaceholder('월 금액 (원)').fill('300000');
    await page.getByRole('button', { name: '다음' }).click();

    await expect(page.getByText('카드 지출 및 결제 유형을 설정해 주세요')).toBeVisible();
    await page.getByRole('button', { name: '생활/식비' }).click();
    await page.getByRole('button', { name: '교통' }).click();
    await page.getByRole('button', { name: '+ 카드 지출 추가' }).click();
    await page.getByPlaceholder('월 금액 (원)').fill('150000');

    const assetLinkRequest = page.waitForRequest((request) => {
      return request.url().includes('/api/users/me/asset-link') && request.method() === 'POST';
    });

    await page.getByRole('button', { name: '마이데이터 연동하기' }).click();

    const request = await assetLinkRequest;
    expect(JSON.parse(request.postData() ?? '{}')).toEqual({
      mainAccountBalanceAmount: 5000000,
      salaryDayOfMonth: 25,
      monthlySalaryAmount: 3000000,
      monthlyFixedExpenseAmount: 1000000,
      jobType: 'LARGE_BIZ',
      depositItems: [{ name: '국민은행 적금', amount: 2000000 }],
      loanItems: [{ name: '전세대출', amount: 10000000 }],
      otherIncomeItems: [{ name: '유튜브', amount: 300000 }],
      cardSpendItems: [{ category: 'LIVING', amount: 150000 }],
      paymentTypes: ['LIVING', 'TRANSPORT'],
    });

    await expect(page.getByText('GAME START')).toBeVisible({ timeout: 15000 });
    await expect(page.getByText('시드머니 저축 계좌')).toBeVisible({ timeout: 15000 });
    await expect(page.getByText('소비 통제 PASS')).toBeVisible({ timeout: 15000 });
  });

  test('submits large won amounts without truncating the asset-link payload', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockAssetLinkFlow(page, {
      totalAssetAmount: 11300000000,
      netAssetAmount: 8700000000,
      totalDebtAmount: 2600000000,
      monthlyIncome: 3200000000,
      monthlyExpense: 1100000000,
      mainAccountBalance: 5000000000,
      seedmoneyBalance: 1200000000,
    });

    await waitForAssetLinkPageReady(page);

    await page.getByPlaceholder('예: 5000000').fill('5000000000');
    await page.getByPlaceholder('1 ~ 28').fill('25');
    await page.getByPlaceholder('예: 3000000').fill('3200000000');
    await page.getByPlaceholder('예: 1000000').fill('1100000000');
    await page.getByRole('combobox').selectOption('LARGE_BIZ');
    await page.getByRole('button', { name: '다음' }).click();

    await expect(page.getByText('보유 금융 자산 정보를 입력해 주세요')).toBeVisible();
    await page.getByRole('button', { name: '+ 예금 추가' }).click();
    await page.getByPlaceholder('이름 (예: 국민은행 적금)').fill('고액 예금');
    await page.getByPlaceholder('잔액 (원)').fill('3500000000');
    await page.getByRole('button', { name: '+ 대출 추가' }).click();
    await page.getByPlaceholder('이름 (예: 신한 전세대출)').fill('주택담보대출');
    await page.locator('input[placeholder="잔액 (원)"]').nth(1).fill('1900000000');
    await page.getByRole('button', { name: '+ 기타 소득 추가' }).click();
    await page.getByPlaceholder('이름 (예: 유튜브 수익)').fill('임대 수익');
    await page.getByPlaceholder('월 금액 (원)').fill('150000000');
    await page.getByRole('button', { name: '다음' }).click();

    await expect(page.getByText('카드 지출 및 결제 유형을 설정해 주세요')).toBeVisible();
    await page.getByRole('button', { name: '생활/식비' }).click();
    await page.getByRole('button', { name: '교통' }).click();
    await page.getByRole('button', { name: '+ 카드 지출 추가' }).click();
    await page.getByPlaceholder('월 금액 (원)').fill('320000000');

    const assetLinkRequest = page.waitForRequest((request) => {
      return request.url().includes('/api/users/me/asset-link') && request.method() === 'POST';
    });

    await page.getByRole('button', { name: '마이데이터 연동하기' }).click();

    const request = await assetLinkRequest;
    expect(JSON.parse(request.postData() ?? '{}')).toEqual({
      mainAccountBalanceAmount: 5000000000,
      salaryDayOfMonth: 25,
      monthlySalaryAmount: 3200000000,
      monthlyFixedExpenseAmount: 1100000000,
      jobType: 'LARGE_BIZ',
      depositItems: [{ name: '고액 예금', amount: 3500000000 }],
      loanItems: [{ name: '주택담보대출', amount: 1900000000 }],
      otherIncomeItems: [{ name: '임대 수익', amount: 150000000 }],
      cardSpendItems: [{ category: 'LIVING', amount: 320000000 }],
      paymentTypes: ['LIVING', 'TRANSPORT'],
    });

    await expect(page.getByText('GAME START')).toBeVisible({ timeout: 15000 });
  });
});
