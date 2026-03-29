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

test.describe('card catalog api', () => {
  test('loads recommendation and card list data and keeps unsupported flows disabled', async ({ page }) => {
    let cardListRequestCount = 0;
    let recommendationRequestCount = 0;

    await seedAuthenticatedUser(page);

    await page.route('**/api/cards', async (route) => {
      cardListRequestCount += 1;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            cards: [
              {
                cardProductId: 2,
                cardName: '홈런 올데이 카드',
                cardIssuerName: '홈런은행',
                cardDescription: '생활비 할인 카드',
                baselinePerformanceAmount: 300000,
                maxBenefitLimitAmount: 40000,
                cardImageUrl: '',
                activeBenefits: [
                  {
                    categoryId: 'life',
                    categoryName: '생활',
                    categoryDescription: '생활 할인',
                    discountRate: 10,
                    exampleMerchants: ['스타벅스'],
                  },
                ],
              },
            ],
          },
        }),
      });
    });

    await page.route('**/api/cards/recommendations', async (route) => {
      recommendationRequestCount += 1;
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            recommendations: [
              {
                cardProductId: 1,
                cardName: '홈런 추천 카드',
                cardIssuerName: '추천카드사',
                cardDescription: '추천 혜택 카드',
                baselinePerformanceAmount: null,
                maxBenefitLimitAmount: null,
                cardImageUrl: '',
                activeBenefits: [
                  {
                    categoryId: 'mobility',
                    categoryName: '교통',
                    categoryDescription: '대중교통 할인',
                    discountRate: 7,
                    exampleMerchants: ['지하철'],
                  },
                ],
              },
            ],
          },
        }),
      });
    });

    await page.goto('/card');

    await expect(page.getByRole('heading', { name: '카드 추천 조회' })).toBeVisible();
    await expect.poll(() => recommendationRequestCount).toBeGreaterThan(0);
    await expect.poll(() => cardListRequestCount).toBeGreaterThan(0);

    await expect(page.getByText('홈런 추천 카드')).toBeVisible();
    await expect(page.getByText('총 1개의 카드')).toBeVisible();

    await page.getByRole('button', { name: '전체 카드' }).click();
    await expect(page.getByText('홈런 올데이 카드')).toBeVisible();

    await page.getByRole('button', { name: /홈런 올데이 카드/ }).click();
    await expect(page.getByTestId('card-detail')).toBeVisible();
    await expect(page.getByText('생활비 할인 카드')).toBeVisible();
    await expect(page.getByRole('button', { name: '카드 신청 API 준비 중' })).toBeDisabled();

    await page.getByRole('button', { name: /목록으로/ }).click();
    await page.getByRole('button', { name: '내 카드' }).click();

    await expect(page.getByTestId('card-mine-disabled')).toBeVisible();
    await expect(page.getByText('내 카드 API 준비 중')).toBeVisible();
    await expect(page.getByText('보유 카드 조회, 카드 신청, 카드 해지는 이번 티켓 범위에서 지원하지 않습니다.')).toBeVisible();
  });
});
