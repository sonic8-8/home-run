import { expect, test, type Page } from '@playwright/test';

const LIVE_MAP_PATH =
  '/property?e2eRegionCode=24&e2eDistrictCode=24110&e2eDistrictName=%EB%B6%81%EA%B5%AC&e2eCenterLng=126.895&e2eCenterLat=35.2';

async function seedAuthenticatedUser(page: Page) {
  await page.addInitScript(() => {
    window.localStorage.setItem(
      'auth',
      JSON.stringify({
        state: {
          isAuthenticated: true,
          accessToken: 'smoke-access-token',
          refreshToken: 'smoke-refresh-token',
          nickname: '라이브맵 테스터',
          accessTokenExpiresAt: Date.now() + 3_600_000,
        },
        version: 0,
      }),
    );
  });
}

function seedRealEstateRouteState(page: Page) {
  return page.addInitScript(() => {
    window.history.replaceState(
      {
        usr: {
          mode: 'new-game',
        },
        key: 'real-estate-live-map-smoke',
        idx: 0,
      },
      '',
      '/property?e2eRegionCode=24&e2eDistrictCode=24110&e2eDistrictName=%EB%B6%81%EA%B5%AC&e2eCenterLng=126.895&e2eCenterLat=35.2',
    );
  });
}

async function mockDistrictProperties(page: Page) {
  let requested = false;

  await page.route('**/api/games/regions/24/districts/24110/properties', async (route) => {
    requested = true;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        status: 200,
        message: 'OK',
        data: {
          properties: [
            {
              propertyId: 'apt-101',
              name: '래미안 홈런',
              latitude: 37.4979,
              longitude: 127.0276,
              recentPrice: 1250000000,
            },
          ],
        },
      }),
    });
  });

  return () => requested;
}
test.describe('real estate live map smoke', () => {
  test.skip(process.env.PLAYWRIGHT_LIVE_MAP !== '1', 'Set PLAYWRIGHT_LIVE_MAP=1 to run the deployed live-map smoke test.');

  test('renders the deployed naver live map instead of the fallback view', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    const didRequestDistrictProperties = await mockDistrictProperties(page);

    await page.goto(LIVE_MAP_PATH);

    await expect.poll(didRequestDistrictProperties).toBe(true);

    await expect(page.locator('script[src*="oapi.map.naver.com/openapi/v3/maps.js"]')).toHaveCount(1);

    const liveMapRoot = page.getByTestId('naver-live-map');
    if (await liveMapRoot.count()) {
      await expect(page.getByText('Live Map')).toBeVisible();
      await expect(page.getByText('북구 부동산 지도')).toBeVisible();
      await expect(liveMapRoot).toHaveAttribute('data-map-ready', 'true', {
        timeout: 15000,
      });
      const boundingBox = await liveMapRoot.boundingBox();
      expect(boundingBox?.width ?? 0).toBeGreaterThan(400);
      expect(boundingBox?.height ?? 0).toBeGreaterThan(300);
      await expect
        .poll(() => liveMapRoot.evaluate((node) => node.childElementCount), {
          timeout: 15000,
        })
        .toBeGreaterThan(0);
      await expect(page.getByText('Map Fallback')).toHaveCount(0);
      return;
    }

    await expect
      .poll(() => page.evaluate(() => window.naver?.maps !== undefined), {
        timeout: 15000,
      })
      .toBe(true);
  });
});
