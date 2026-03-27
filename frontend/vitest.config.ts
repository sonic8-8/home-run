import { mergeConfig, defineConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: ['./tests/setup.ts'],
      include: ['tests/unit/**/*.{test,spec}.{ts,tsx}'],
      css: true,
      restoreMocks: true,
      clearMocks: true,
    },
  }),
);
