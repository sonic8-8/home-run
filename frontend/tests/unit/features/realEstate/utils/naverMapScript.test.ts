import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  buildNaverMapScriptUrl,
  loadNaverMapScript,
  resetNaverMapScriptLoaderForTests,
} from '@features/realEstate/utils/naverMapScript';

describe('naverMapScript', () => {
  afterEach(() => {
    delete (window as Window & typeof globalThis & { naver?: unknown }).naver;
    resetNaverMapScriptLoaderForTests();
  });

  it('builds the sdk url from the client id', () => {
    expect(buildNaverMapScriptUrl('test-client-id')).toBe(
      'https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=test-client-id',
    );
  });

  it('appends the sdk script only once', async () => {
    let appendedScript!: HTMLScriptElement;
    const appendChildSpy = vi.spyOn(document.head, 'appendChild').mockImplementation((node) => {
      appendedScript = node as HTMLScriptElement;
      return node;
    });

    const promise = loadNaverMapScript('test-client-id');
    const script = appendedScript;

    expect(script.dataset.clientId).toBe('test-client-id');

    script.onload?.(new Event('load'));
    await promise;

    const secondPromise = loadNaverMapScript('test-client-id');
    expect(appendChildSpy).toHaveBeenCalledTimes(1);
    await secondPromise;

    appendChildSpy.mockRestore();
  });

  it('rejects when the client id is blank', async () => {
    await expect(loadNaverMapScript('   ')).rejects.toThrow(
      '네이버 지도 설정이 누락되었습니다.',
    );
  });
});
