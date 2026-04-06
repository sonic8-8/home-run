import type { Page } from '@playwright/test';

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
          [129.0, 35.03],
          [129.2, 35.03],
          [129.2, 35.22],
          [129.0, 35.22],
          [129.0, 35.03],
        ]],
      },
    },
  ],
} as const;

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
          [127.1, 37.48],
          [127.1, 37.55],
          [127.01, 37.55],
          [127.01, 37.48],
        ]],
      },
    },
  ],
} as const;

type SeedRouteStateOptions = {
  pathname: string;
  state: unknown;
  key: string;
};

export async function seedAuthenticatedUser(page: Page) {
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

export function seedRouteState(page: Page, { pathname, state, key }: SeedRouteStateOptions) {
  return page.addInitScript((payload) => {
    window.history.replaceState(
      {
        usr: payload.state,
        key: payload.key,
        idx: 0,
      },
      '',
      payload.pathname,
    );
  }, {
    pathname,
    state,
    key,
  });
}

export function seedRealEstateRouteState(page: Page) {
  return seedRouteState(page, {
    pathname: '/property/new-game',
    key: 'real-estate-visibility-test',
    state: {
      slotNumber: 1,
      characterType: 'MALE',
      characterName: '테스터',
      jobType: 'LARGE_BIZ',
      useMyData: false,
    },
  });
}

export async function mockMapGeographies(page: Page) {
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

export async function mockExternalScripts(page: Page) {
  await page.route('**/openapi/v3/maps.js*', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/javascript',
      body: '',
    });
  });
}
