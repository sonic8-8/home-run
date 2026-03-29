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
      { usr: nextState, key: 'stock-api-flow-test', idx: 0 },
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

test.describe('stock api flow', () => {
  test('loads the market and submits an order from the game main stock panel', async ({ page }) => {
    const sessionId = 41;
    const currentDate = '2026-03-30';
    let orderPayload: Record<string, unknown> | null = null;
    let orderPlaced = false;

    await seedAuthenticatedUser(page);
    await seedNewsSeenDate(page, sessionId, currentDate);
    await seedRouteState(page, '/game', {
      sessionId,
      characterType: 'MALE',
    });
    await mockGameSessionBase(page, sessionId, currentDate);

    await page.route(`**/api/games/sessions/${sessionId}/stocks/market`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            stocks: [
              {
                stockCode: '005930',
                stockName: '삼성전자',
                currentPrice: 75000,
                pricePerShare: '주당 75,000원',
              },
              {
                stockCode: '000660',
                stockName: 'SK하이닉스',
                currentPrice: 120000,
                pricePerShare: '주당 120,000원',
              },
            ],
          },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/stocks/holdings`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: orderPlaced
            ? {
                totalValue: 150000,
                totalReturnRate: 7.1,
                totalPurchaseAmount: 140000,
                holdings: [
                  {
                    stockCode: '005930',
                    stockName: '삼성전자',
                    currentValue: 150000,
                    quantity: 2,
                    avgPurchasePrice: 70000,
                    returnRate: 7.1,
                  },
                ],
              }
            : {
                totalValue: 0,
                totalReturnRate: 0,
                totalPurchaseAmount: 0,
                holdings: [],
              },
        }),
      });
    });

    await page.route(`**/api/games/sessions/${sessionId}/stocks/orders`, async (route) => {
      orderPayload = route.request().postDataJSON() as Record<string, unknown>;
      orderPlaced = true;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            orderId: 901,
            stockCode: '005930',
            orderType: 'BUY',
            quantity: 2,
            pricePerShare: 75000,
            totalAmount: 150000,
            executeTurn: '턴 2',
            orderStatus: 'PENDING',
          },
        }),
      });
    });

    await page.goto('/game');
    await waitForRouteHydration(page);

    await page.getByRole('button', { name: '주식 투자' }).click();

    await expect(page.getByRole('heading', { name: '주식 투자' })).toBeVisible();
    await expect(page.getByRole('button', { name: /삼성전자/ })).toBeVisible();
    await expect(page.getByText('보유 중인 주식이 없습니다.')).toBeVisible();

    await page.getByRole('button', { name: /삼성전자/ }).click();
    await page.getByLabel('주문 수량').fill('2');
    await page.getByRole('button', { name: '주문 요청' }).click();

    await expect.poll(() => orderPayload).toEqual({
      stockCode: '005930',
      orderType: 'BUY',
      quantity: 2,
    });

    await expect(page.getByTestId('stock-order-result')).toBeVisible();
    await expect(page.getByText('총 주문 금액 150,000 원 · 주문 접수')).toBeVisible();
    await expect(page.getByText('2주 보유')).toBeVisible();
  });
});
