import { describe, expect, it } from 'vitest';
import { normalizeApiBaseUrl } from './baseUrl';

describe('normalizeApiBaseUrl', () => {
  it('빈 base url이면 same-origin /api를 사용한다', () => {
    expect(normalizeApiBaseUrl('')).toBe('/api');
  });

  it('끝 슬래시를 제거하고 /api를 붙인다', () => {
    expect(normalizeApiBaseUrl('http://127.0.0.1:8081/')).toBe('http://127.0.0.1:8081/api');
  });

  it('이미 /api면 그대로 유지한다', () => {
    expect(normalizeApiBaseUrl('https://example.com/api')).toBe('https://example.com/api');
  });
});
