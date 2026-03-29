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
          nickname: '커리어테스터',
          accessTokenExpiresAt: Date.now() + 3_600_000,
        },
        version: 0,
      }),
    );
  });
}

test.describe('career api flow', () => {
  test('loads the active session, negotiates salary, and accepts a job transfer', async ({ page }) => {
    let transferCompleted = false;

    await seedAuthenticatedUser(page);

    await page.route('**/api/games/sessions', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            sessions: [
              {
                sessionId: 52,
                slotNumber: 2,
                characterName: '서연',
                jobType: 'SMALL_BIZ',
                currentTurn: 9,
                status: 'IN_PROGRESS',
              },
            ],
          },
        }),
      });
    });

    await page.route('**/api/games/sessions/52/career/job-offers', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            offers: transferCompleted
              ? []
              : [
                  {
                    offerId: 'offer-1',
                    jobType: 'LARGE_BIZ',
                    companyName: '홈런전자',
                    currentSalary: 48000000,
                    offeredSalary: 56000000,
                    probationTurns: 2,
                  },
                ],
            offerChanceBonusRate: 10,
            meetFriendBonusApplied: true,
          },
        }),
      });
    });

    await page.route('**/api/games/sessions/52/career/negotiate', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            success: true,
            previousSalary: 48000000,
            newSalary: 52800000,
            raiseRate: 10,
            lastNegotiatedTurn: 9,
            message: '연봉 협상이 성공적으로 반영되었습니다.',
          },
        }),
      });
    });

    await page.route('**/api/games/sessions/52/career/transfer', async (route) => {
      const body = route.request().postDataJSON();

      expect(body).toEqual({ offerId: 'offer-1' });
      transferCompleted = true;

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          status: 200,
          message: 'OK',
          data: {
            previousJobType: 'SMALL_BIZ',
            newJobType: 'LARGE_BIZ',
            newJobTitle: '플랫폼 PM',
            newSalary: 56000000,
            probationEndTurn: 11,
            tenureReset: true,
            message: '이직이 완료되었습니다.',
          },
        }),
      });
    });

    await page.goto('/mypage');

    await expect(page.getByRole('heading', { name: '커리어 협상과 이직 관리' })).toBeVisible();
    await expect(page.getByText('서연')).toBeVisible();
    await expect(page.getByText('홈런전자')).toBeVisible();
    await expect(page.getByText('제안 보너스 10%')).toBeVisible();

    await page.getByRole('button', { name: '연봉 협상 진행' }).click();

    await expect(page.getByTestId('career-negotiation-result')).toBeVisible();
    await expect(page.getByText('연봉 협상이 성공적으로 반영되었습니다.')).toBeVisible();
    await expect(
      page.getByTestId('career-negotiation-result').getByText('52,800,000원'),
    ).toBeVisible();

    await page.getByRole('button', { name: '이직 수락' }).click();

    await expect(page.getByTestId('career-transfer-result')).toBeVisible();
    await expect(page.getByText('플랫폼 PM')).toBeVisible();
    await expect(page.getByText('이직이 완료되었습니다.')).toBeVisible();
    await expect(page.getByTestId('career-offers-empty-state')).toBeVisible();
  });
});
