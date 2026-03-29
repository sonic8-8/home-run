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

function seedRouteState(
  page: Page,
  path: string,
  state: Record<string, unknown>,
) {
  return page.addInitScript(({ nextPath, nextState }) => {
    window.history.replaceState(
      { usr: nextState, key: 'loan-api-flow-test', idx: 0 },
      '',
      nextPath,
    );
  }, { nextPath: path, nextState: state });
}

function seedNewsSeenDate(page: Page, sessionId: number, date: string) {
  return page.addInitScript(({ nextSessionId, nextDate }) => {
    window.sessionStorage.setItem(`news_seen_date_${nextSessionId}`, nextDate);
  }, { nextSessionId: sessionId, nextDate: date });
}

async function waitForRouteHydration(page: Page) {
  await expect(
    page.getByText('불러오는 중...', { exact: true }),
  ).toHaveCount(0, {
    timeout: 15_000,
  });
}

async function mockGameSessionBase(page: Page, sessionId: number, currentDate: string) {
  await page.route(`**/api/games/sessions/${sessionId}`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          sessionId,
          slotNumber: 1,
          currentTurn: 1,
          currentDate,
          status: 'IN_PROGRESS',
          createdAt: currentDate,
          endedAt: null,
        },
      }),
    });
  });

  await page.route(`**/api/games/sessions/${sessionId}/turn`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          turnNumber: 1,
          currentDate,
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

test.describe('loan api flow', () => {
  test('loads loan products and calculates repayment from the in-game loan panel', async ({ page }) => {
    const sessionId = 31;
    const currentDate = '2026-03-27';
    let calculatePayload: Record<string, unknown> | null = null;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'FEMALE',
      openLoan: true,
    });
    await mockGameSessionBase(page, sessionId, currentDate);

    await page.route(`**/api/games/sessions/${sessionId}/loans/products**`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: [
            {
              productId: 'LN-001',
              bankName: '홈런은행',
              bankLogoUrl: '',
              productName: '홈런 주택담보 대출',
              productType: 'MORTGAGE',
              minRate: 3.15,
              maxRate: 4.1,
            },
          ],
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/loans/products/LN-001`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            productId: 'LN-001',
            bankName: '홈런은행',
            bankLogoUrl: '',
            productName: '홈런 주택담보 대출',
            productType: 'MORTGAGE',
            minRate: 3.15,
            maxRate: 4.1,
            features: ['담보 가치 기반 심사', '중도상환 수수료 없음', '최대 30년 상환'],
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/loans/calculate`, async (route) => {
      calculatePayload = route.request().postDataJSON() as Record<string, unknown>;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            monthlyPayment: 2_413_000,
            totalInterest: 168_680_000,
            totalPayment: 868_680_000,
          },
        }),
      });
    });

    await page.goto('/game');
    await waitForRouteHydration(page);

    await expect(page.getByRole('heading', { name: '대출/상환' })).toBeVisible();
    await expect(page.getByRole('button', { name: /홈런 주택담보 대출/ })).toBeVisible();

    await page.getByRole('button', { name: /홈런 주택담보 대출/ }).click();

    await expect(page.getByText('이자 계산기')).toBeVisible();
    await page.getByPlaceholder('금액').fill('700000000');
    await page.getByRole('button', { name: '계산 하기' }).click();

    await expect.poll(() => calculatePayload).not.toBeNull();
    expect(calculatePayload).toEqual({
      repaymentMethod: 'EQUAL_PRINCIPAL_INTEREST',
      termMonths: 12,
      principal: 700000000,
      annualRate: 3.15,
    });

    await expect(page.getByText('월 납입금')).toBeVisible();
    await expect(page.getByText('2,413,000 원')).toBeVisible();
    await expect(page.getByText('총 상환액 868,680,000 원')).toBeVisible();
  });

  test('submits loan review and confirm requests from the real-estate apply flow', async ({ page }) => {
    const sessionId = 31;
    const currentDate = '2026-03-27';
    let applyPayload: Record<string, unknown> | null = null;
    let confirmPayload: Record<string, unknown> | null = null;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/property', {
      mode: 'loan-apply',
      sessionId,
      productId: 'LN-001',
      preSelectedPropertyId: 'property-77',
      preSelectedPropertyName: '잠실 홈런캐슬',
      preSelectedPropertyPrice: 950000000,
    });
    await mockGameSessionBase(page, sessionId, currentDate);

    await page.route(`**/api/games/sessions/${sessionId}/loans/apply`, async (route) => {
      applyPayload = route.request().postDataJSON() as Record<string, unknown>;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            applicationId: 88,
            status: 'APPROVED',
            requestInfo: {
              applicationDate: '2026-03-27 09:00:00',
            },
            result: {
              maxLoanAmount: 600000000,
            },
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/loans/confirm`, async (route) => {
      confirmPayload = route.request().postDataJSON() as Record<string, unknown>;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            loanId: 501,
            amount: 500000000,
            annualRate: 3.15,
            monthlyPayment: 2413000,
            contractDate: '2026-03-27',
            status: 'ACTIVE',
          },
        }),
      });
    });

    await page.goto('/property');
    await waitForRouteHydration(page);

    await expect.poll(() => applyPayload).not.toBeNull();
    expect(applyPayload).toEqual({
      productId: 'LN-001',
      propertyId: 'property-77',
    });

    await expect(page.getByRole('heading', { name: '대출 심사 이력 및 상세' })).toBeVisible();
    await expect(page.getByText('잠실 홈런캐슬')).toBeVisible();
    await expect(page.getByText('최대 대출 가능 금액')).toBeVisible();

    await page.getByRole('button', { name: '부동산 계약하러 가기' }).click();

    await expect(page.getByPlaceholder('금액을 입력해주세요')).toBeVisible();
    await page.getByPlaceholder('금액을 입력해주세요').fill('500000000');
    await page.getByRole('button', { name: '부동산 계약하러 가기' }).click();

    await expect.poll(() => confirmPayload).not.toBeNull();
    expect(confirmPayload).toEqual({
      applicationId: 88,
      requestedAmount: 500000000,
      agreed: true,
    });

    await expect(page).toHaveURL(/\/game$/);
    await expect(page.getByTestId('active-loan-card')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('확정된 대출')).toBeVisible();
  });

  test('repays an active loan from the in-game loan panel', async ({ page }) => {
    const sessionId = 31;
    const currentDate = '2026-03-27';
    let repayPayload: Record<string, unknown> | null = null;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'FEMALE',
      openLoan: true,
      confirmedLoan: {
        loanId: 501,
        amount: 500000000,
        annualRate: 3.15,
        monthlyPayment: 2413000,
        contractDate: '2026-03-27',
        status: 'ACTIVE',
      },
    });
    await mockGameSessionBase(page, sessionId, currentDate);

    await page.route(`**/api/games/sessions/${sessionId}/loans/products**`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: [],
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/loans/repay`, async (route) => {
      repayPayload = route.request().postDataJSON() as Record<string, unknown>;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            loanId: 501,
            repaidAmount: 100000000,
            remainingPrincipal: 400000000,
            updatedMonthlyPayment: 1930000,
          },
        }),
      });
    });

    await page.goto('/game');
    await waitForRouteHydration(page);

    await expect(page.getByTestId('active-loan-card')).toBeVisible();
    await expect(page.getByText('500,000,000원')).toBeVisible();

    await page.getByPlaceholder('상환할 금액 입력').fill('100000000');
    await page.getByRole('button', { name: '상환하기' }).click();

    await expect.poll(() => repayPayload).not.toBeNull();
    expect(repayPayload).toEqual({
      loanId: 501,
      amount: 100000000,
    });

    await expect(page.getByText('400,000,000원')).toBeVisible();
    await expect(page.getByText('1,930,000원')).toBeVisible();
  });
});
