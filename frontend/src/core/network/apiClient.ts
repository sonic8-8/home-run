import axios from 'axios';
import { setupInterceptors } from './interceptors';
import { normalizeApiBaseUrl } from './baseUrl';

const NORMALIZED_BASE_URL = normalizeApiBaseUrl(
  import.meta.env.VITE_API_BASE_URL ?? '',
);

export const apiClient = axios.create({
  baseURL: NORMALIZED_BASE_URL,
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
});

setupInterceptors(apiClient);
