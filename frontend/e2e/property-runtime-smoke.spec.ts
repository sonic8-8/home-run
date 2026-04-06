import { expect, test } from '@playwright/test';
import {
  mockMapGeographies,
  seedAuthenticatedUser,
  seedRealEstateRouteState,
} from './support/runtimeFixtures';

test.describe('property runtime smoke', () => {
  test('keeps the country map labels and active regions visible in runtime mode', async ({ page }) => {
    await seedAuthenticatedUser(page);
    await seedRealEstateRouteState(page);
    await mockMapGeographies(page);

    await page.goto('/property/new-game');

    await expect(page.getByText('지역을 먼저 고르세요')).toBeVisible();
    await expect(page.getByText('세종')).toBeVisible();
    await expect(page.getByText('부산')).toBeVisible();
    await expect(page.getByTestId('country-region-11')).toBeVisible();
    await expect(page.getByTestId('country-region-29')).toBeVisible();
  });
});
