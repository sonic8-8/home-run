const API_SUFFIX = '/api';

function trimTrailingSlash(value: string): string {
  if (value.endsWith('/')) {
    return value.slice(0, -1);
  }

  return value;
}

export function normalizeApiBaseUrl(baseUrl: string): string {
  const normalizedBaseUrl = trimTrailingSlash(baseUrl);

  if (normalizedBaseUrl.endsWith(API_SUFFIX)) {
    return normalizedBaseUrl;
  }

  return `${normalizedBaseUrl}${API_SUFFIX}`;
}
