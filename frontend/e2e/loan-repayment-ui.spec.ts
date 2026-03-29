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
          nickname: '대출테스터',
          accessTokenExpiresAt: Date.now() + 3_600_000,
        },
        version: 0,
      }),
    );
  });
}

function seedRouteState(page: Page, path: string, state: Record<string, unknown>) {
  return page.addInitScript(({ nextPath, nextState }) => {
    window.history.replaceState(
      { usr: nextState, key: 'loan-repayment-ui-test', idx: 0 },
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

test.describe('loan repayment ui', () => {
  test('shows repayment controls when a confirmed loan already exists', async ({ page }) => {
    const sessionId = 31;
    const currentDate = '2026-03-27';

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

    await page.goto('/game');

    await expect(page.getByTestId('active-loan-card')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('확정된 대출')).toBeVisible();
    await expect(page.getByText('500,000,000원')).toBeVisible();
    await expect(page.getByText('2,413,000원')).toBeVisible();
    await expect(page.getByPlaceholder('상환할 금액 입력')).toBeVisible();
    await expect(page.getByRole('button', { name: '상환하기' })).toBeVisible();
  });
});
