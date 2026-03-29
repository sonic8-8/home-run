import { expect, test, type Page } from '@playwright/test';
import type { EndingType } from '@features/ending/domain/entities/EndingReport';

test.describe.configure({ mode: 'serial' });

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
            nickname: '엔딩테스터',
            accessTokenExpiresAt: Date.now() + 60 * 60 * 1000,
          },
          version: 0,
        }),
      );
    } catch {
      // Playwright init scripts also run on documents like about:blank where localStorage is unavailable.
    }
  });
}

async function mockEndingApis(
  page: Page,
  options: {
    sessionId?: number;
    endingType?: EndingType;
    title?: string;
    characterType?: 'FEMALE' | 'MALE';
  } = {},
) {
  const {
    sessionId = 10,
    endingType = 'CLEAR',
    title = '내 집 마련 성공',
    characterType = 'FEMALE',
  } = options;

  await page.route(`**/api/games/sessions/${sessionId}/ending`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          characterType,
          endingType,
          grade: 'S',
          title,
          totalAssets: 350000000,
          totalIncome: 220000000,
          totalExpense: 90000000,
          netProfit: 130000000,
          spendingPattern: {
            topCategory: '주거',
            topCategoryRatio: 0.41,
          },
          achievements: [],
          newsHistories: [
            {
              turnNumber: 4,
              newsId: 'NEWS-1',
              headline: '금리 인하',
              publishedDate: '2026-03-01',
            },
            {
              turnNumber: 7,
              newsId: 'NEWS-2',
              headline: '부동산 규제 완화',
              publishedDate: '2026-06-01',
            },
          ],
          eventHistories: [
            {
              turnNumber: 6,
              gameEventId: 11,
              selectedChoiceCode: 'A',
              resultSummary: '연봉 협상 성공',
              resolvedAt: '2026-05-01T00:00:00',
            },
            {
              turnNumber: 9,
              gameEventId: 17,
              selectedChoiceCode: 'B',
              resultSummary: '이직 제안을 거절했다',
              resolvedAt: '2026-08-01T00:00:00',
            },
          ],
          housingHistories: [
            {
              turnNumber: 8,
              summary: '자가 구매',
              beforeState: {
                housingType: 'JEONSE_APT',
                propertyId: 20,
              },
              afterState: {
                housingType: 'OWNED_APT',
                propertyId: 30,
              },
            },
            {
              turnNumber: 5,
              summary: '전세 입주',
              beforeState: {
                housingType: 'VILLA',
                propertyId: 10,
              },
              afterState: {
                housingType: 'JEONSE_APT',
                propertyId: 20,
              },
            },
          ],
          housingSnapshot: {
            currentHousingType: 'OWNED_APT',
            currentPropertyId: 30,
            targetPropertyId: 30,
          },
        },
      }),
    });
  });

  await page.route(`**/api/games/sessions/${sessionId}/logs`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          timeline: [
            {
              turnNumber: 1,
              date: '2026-01-01',
              cash: 1000000,
              netAssets: 2000000,
              totalAssets: 3000000,
              stockValue: 0,
              loanBalance: 0,
              salary: 2500000,
            },
            {
              turnNumber: 2,
              date: '2026-02-01',
              cash: 2000000,
              netAssets: 2400000,
              totalAssets: 3400000,
              stockValue: 100000,
              loanBalance: 0,
              salary: 2500000,
            },
          ],
        },
      }),
    });
  });
}

test.describe('ending page', () => {
  test('renders the integrated ending summary and history flow', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await mockEndingApis(page);

    await page.goto('/game/10/ending');

    await expect(page.getByTestId('ending-page')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByTestId('ending-scene')).toContainText('내 집 마련 성공', { timeout: 15_000 });
    await expect(page.getByTestId('ending-reveal-report')).toContainText('>> 결과 보기', { timeout: 15_000 });

    await page.getByTestId('ending-reveal-report').click();

    await expect(page.getByTestId('ending-summary')).toContainText('내 집 마련 성공', { timeout: 15_000 });
    await expect(page.getByTestId('ending-summary')).toContainText('목표 달성', { timeout: 15_000 });
    await expect(page.getByTestId('ending-timeline')).toContainText('총 2개의 월별 로그가 준비되었습니다.', { timeout: 15_000 });
    await expect(page.getByTestId('ending-news-history')).toContainText('금리 인하', { timeout: 15_000 });
    await expect(page.getByTestId('ending-news-history')).toContainText('부동산 규제 완화', { timeout: 15_000 });
    await expect(page.getByTestId('ending-event-history')).toContainText('연봉 협상 성공', { timeout: 15_000 });
    await expect(page.getByTestId('ending-event-history')).toContainText('이직 제안을 거절했다', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('자가 구매', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('전세 입주', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('현재 주거', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('자가', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('목표 주거 달성', { timeout: 15_000 });
    await expect(page.getByTestId('ending-housing-history')).toContainText('달성', { timeout: 15_000 });
  });

  for (const [endingType, title] of [
    ['CLEAR', '내 집 마련 성공'],
    ['BANKRUPT', '파산'],
    ['TIMEOUT', '기한 초과'],
    ['FORECLOSURE', '차압'],
  ] satisfies [EndingType, string][]) {
    test(`renders ${endingType} scene`, async ({ page }) => {
      await seedAuthenticatedUser(page);
      await mockEndingApis(page, { sessionId: 20, endingType, title });

      await page.goto('/game/20/ending');

      await expect(page.getByTestId('ending-scene')).toContainText(title, { timeout: 15_000 });
      await expect(page.getByTestId('ending-reveal-report')).toContainText('>> 결과 보기', { timeout: 15_000 });
    });
  }

  test('shows the not-ready state when ending report APIs return 409', async ({ page }) => {
    await seedAuthenticatedUser(page);

    await page.route('**/api/games/sessions/11/ending', async (route) => {
      await route.fulfill({
        status: 409,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 409,
          message: '엔딩 리포트가 아직 생성되지 않았습니다.',
        }),
      });
    });
    await page.route('**/api/games/sessions/11/logs', async (route) => {
      await route.fulfill({
        status: 409,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 409,
          message: '엔딩 리포트가 아직 생성되지 않았습니다.',
        }),
      });
    });

    await page.goto('/game/11/ending');

    await expect(page.getByTestId('ending-not-ready')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText('엔딩 리포트 준비 중')).toBeVisible({ timeout: 15_000 });
  });
});
