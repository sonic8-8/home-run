import path from 'node:path';

import { defineConfig, devices } from '@playwright/test';

const playwrightPort = process.env.PLAYWRIGHT_PORT ?? '4174';
const configuredBaseUrl = process.env.PLAYWRIGHT_BASE_URL?.trim();
const playwrightBaseUrl =
  configuredBaseUrl && configuredBaseUrl.length > 0
    ? configuredBaseUrl
    : `http://127.0.0.1:${playwrightPort}`;
const workerCount = Number(process.env.PLAYWRIGHT_WORKERS ?? '1');
const playwrightArtifactsDir = process.env.PLAYWRIGHT_ARTIFACTS_DIR;
const playwrightReuseExistingServer = process.env.PLAYWRIGHT_REUSE_EXISTING_SERVER === '1';
const playwrightWebServerTimeout = Number(process.env.PLAYWRIGHT_WEB_SERVER_TIMEOUT ?? '120000');
const playwrightWebServerCommand =
  process.env.PLAYWRIGHT_WEB_SERVER_COMMAND ?? `npm run dev -- --host 127.0.0.1 --port ${playwrightPort}`;
const shouldSkipWebServer = process.env.PLAYWRIGHT_SKIP_WEB_SERVER === '1';
const shouldIncludeLiveMapSpecs = process.env.PLAYWRIGHT_LIVE_MAP === '1';

const htmlReportOutputFolder = playwrightArtifactsDir
  ? path.join(playwrightArtifactsDir, 'playwright-report')
  : 'playwright-report';
const testOutputDir = playwrightArtifactsDir
  ? path.join(playwrightArtifactsDir, 'test-results')
  : 'test-results';

export default defineConfig({
  testDir: './e2e',
  testIgnore: shouldIncludeLiveMapSpecs ? [] : ['**/live/**'],
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: workerCount,
  reporter: [['list'], ['html', { open: 'never', outputFolder: htmlReportOutputFolder }]],
  outputDir: testOutputDir,
  use: {
    baseURL: playwrightBaseUrl,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: shouldSkipWebServer
    ? undefined
    : {
        command: playwrightWebServerCommand,
        url: playwrightBaseUrl,
        reuseExistingServer: playwrightReuseExistingServer,
        timeout: playwrightWebServerTimeout,
      },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
      },
    },
  ],
});
