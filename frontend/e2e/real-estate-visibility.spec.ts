import { expect, test, type Page } from '@playwright/test';

const PROVINCES_GEOJSON = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: {
        name: '서울특별시',
        code: '11',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [126.84, 37.45],
          [127.16, 37.45],
          [127.16, 37.72],
          [126.84, 37.72],
          [126.84, 37.45],
        ]],
      },
    },
    {
      type: 'Feature',
      properties: {
        name: '광주광역시',
        code: '24',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [126.75, 35.08],
          [126.95, 35.08],
          [126.95, 35.25],
          [126.75, 35.25],
          [126.75, 35.08],
        ]],
      },
    },
    {
      type: 'Feature',
      properties: {
        name: '세종특별자치시',
        code: '29',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [127.15, 36.42],
          [127.34, 36.42],
          [127.34, 36.58],
          [127.15, 36.58],
          [127.15, 36.42],
        ]],
      },
    },
    {
      type: 'Feature',
      properties: {
        name: '부산광역시',
        code: '26',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [129.00, 35.03],
          [129.20, 35.03],
          [129.20, 35.22],
          [129.00, 35.22],
          [129.00, 35.03],
        ]],
      },
    },
  ],
};

const MUNICIPALITIES_GEOJSON = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: {
        name: '종로구',
        code: '11110',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [126.96, 37.55],
          [127.01, 37.55],
          [127.01, 37.61],
          [126.96, 37.61],
          [126.96, 37.55],
        ]],
      },
    },
    {
      type: 'Feature',
      properties: {
        name: '강남구',
        code: '11680',
      },
      geometry: {
        type: 'Polygon',
        coordinates: [[
          [127.01, 37.48],
          [127.10, 37.48],
          [127.10, 37.55],
          [127.01, 37.55],
          [127.01, 37.48],
        ]],
      },
    },
  ],
};

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

function seedRealEstateRouteState(page: Page) {
  return page.addInitScript(() => {
    window.history.replaceState(
      {
        usr: {
          mode: 'new-game',
        },
        key: 'real-estate-visibility-test',
        idx: 0,
      },
      '',
      '/property',
    );
  });
}

async function mockMapGeographies(page: Page) {
  await page.route('**/skorea-provinces-2018-topo-simple.json', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(PROVINCES_GEOJSON),
    });
  });

  await page.route('**/skorea-municipalities-2018-topo-simple.json', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(MUNICIPALITIES_GEOJSON),
    });
  });
}

async function mockDistrictProperties(page: Page) {
  await page.route('**/api/games/regions/11/districts/11680/properties', async (route) => {
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
            {
              propertyId: 'apt-202',
              name: '자이 플레이스',
              latitude: 37.503,
              longitude: 127.041,
              recentPrice: 980000000,
            },
          ],
        },
      }),
    });
  });
}

async function triggerMapSelection(page: Page, testId: string) {
  await page.getByTestId(testId).dispatchEvent('click');
}

test.describe('real estate visibility', () => {
  test('shows the staged guidance card while narrowing from country to city', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);

    await page.goto('/property');

    await expect(page.getByText('Real Estate Map')).toBeVisible();
    await expect(page.getByText('지역을 먼저 고르세요')).toBeVisible();
    await expect(page.getByText('지역 탐색 단계')).toBeVisible();
    await expect(page.getByText('세종')).toBeVisible();
    await expect(page.getByText('부산')).toBeVisible();
    await expect(page.getByTestId('country-region-11')).toBeVisible();
    await expect(page.getByTestId('country-region-29')).toBeVisible();

    await triggerMapSelection(page, 'country-region-11');

    await expect(page.getByText('서울에서 구를 선택하세요')).toBeVisible();
    await expect(page.getByText('대한민국 › 서울').first()).toBeVisible();
  });

  test('shows the fallback property list and summary panel in district view on localhost', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);
    await mockDistrictProperties(page);

    await page.goto('/property');

    await triggerMapSelection(page, 'country-region-11');
    await triggerMapSelection(page, 'city-gu-11680');

    await expect(page.getByText('Map Fallback')).toBeVisible();
    await expect(page.getByText('강남구 매물 목록').first()).toBeVisible();
    await expect(page.getByRole('button', { name: '래미안 홈런' })).toBeVisible();

    await page.getByRole('button', { name: '래미안 홈런' }).click();

    await expect(page.getByText('목표 매물 정보')).toBeVisible();
    await expect(page.getByText('래미안 홈런').first()).toBeVisible();
    await expect(page.getByRole('button', { name: '이 매물로 시작하기' })).toBeVisible();
  });
});
