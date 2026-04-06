import { expect, test } from '@playwright/test';

import {
  mockMapGeographies,
  seedAuthenticatedUser,
  seedRealEstateRouteState,
} from './support/runtimeFixtures';

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

function parseRgb(rgb: string): [number, number, number] {
  const match = rgb.match(/\d+/g);
  if (!match || match.length < 3) {
    throw new Error(`RGB 값을 파싱할 수 없습니다: ${rgb}`);
  }

  return [Number(match[0]), Number(match[1]), Number(match[2])];
}

function relativeLuminance([r, g, b]: [number, number, number]): number {
  const normalized = [r, g, b].map((value) => {
    const channel = value / 255;
    return channel <= 0.03928
      ? channel / 12.92
      : ((channel + 0.055) / 1.055) ** 2.4;
  });

  return 0.2126 * normalized[0] + 0.7152 * normalized[1] + 0.0722 * normalized[2];
}

function contrastRatio(foreground: string, background: string): number {
  const foregroundLuminance = relativeLuminance(parseRgb(foreground));
  const backgroundLuminance = relativeLuminance(parseRgb(background));
  const lighter = Math.max(foregroundLuminance, backgroundLuminance);
  const darker = Math.min(foregroundLuminance, backgroundLuminance);
  return (lighter + 0.05) / (darker + 0.05);
}

test.describe('real estate visibility', () => {
  test('shows the staged guidance card while narrowing from country to city', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);

    await page.goto('/property/new-game');

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

  test('keeps inactive province labels readable in country view', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);

    await page.goto('/property/new-game');

    for (const label of ['세종', '부산']) {
      const provinceLabel = page.locator('svg text').filter({ hasText: label }).first();
      await expect(provinceLabel).toBeVisible();

      const fill = await provinceLabel.evaluate((element) => getComputedStyle(element).fill);
      expect(contrastRatio(fill, 'rgb(255, 255, 255)')).toBeGreaterThanOrEqual(2.5);
    }
  });

  test('shows the fallback property list and summary panel in district view on localhost', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);
    await mockDistrictProperties(page);

    await page.goto('/property/new-game');

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
