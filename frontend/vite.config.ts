import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

const viteCacheDir = path.resolve(
  process.env.TMPDIR ?? process.env.TEMP ?? process.env.TMP ?? '/tmp',
  's14p21c103-frontend-vite',
)
const defaultApiProxyTarget = process.env.VITE_DEV_API_PROXY_TARGET ?? 'http://127.0.0.1:8081'

// https://vite.dev/config/
export default defineConfig({
  cacheDir: viteCacheDir,
  plugins: [
    react({
      babel: {
        plugins: [
          "babel-plugin-transform-typescript-metadata",
          ["@babel/plugin-proposal-decorators", { legacy: true }],
        ],
      },
    }),
  ],
  server: {
    proxy: {
      '/api': defaultApiProxyTarget,
    },
  },
  resolve: {
    alias: {
      '@':         path.resolve(__dirname, 'src'),
      '@app':      path.resolve(__dirname, 'src/app'),
      '@features': path.resolve(__dirname, 'src/features'),
      '@shared':   path.resolve(__dirname, 'src/shared'),
      '@core':     path.resolve(__dirname, 'src/core'),
      '@assets':   path.resolve(__dirname, 'src/assets'),
    },
  },
});
