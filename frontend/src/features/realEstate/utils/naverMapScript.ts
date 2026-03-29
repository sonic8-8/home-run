const NAVER_MAP_SCRIPT_ID = 'naver-map-sdk';
const NAVER_MAP_SCRIPT_BASE_URL = 'https://oapi.map.naver.com/openapi/v3/maps.js';

let naverMapScriptPromise: Promise<void> | null = null;

type WindowWithNaver = Window & typeof globalThis & {
  naver?: {
    maps?: unknown;
  };
};

function hasNaverMapSdk(): boolean {
  return (window as WindowWithNaver).naver?.maps !== undefined;
}

export function buildNaverMapScriptUrl(clientId: string): string {
  const trimmedClientId = clientId.trim();
  return `${NAVER_MAP_SCRIPT_BASE_URL}?ncpKeyId=${encodeURIComponent(trimmedClientId)}`;
}

export function loadNaverMapScript(clientId: string): Promise<void> {
  const trimmedClientId = clientId.trim();

  if (trimmedClientId.length === 0) {
    return Promise.reject(new Error('네이버 지도 설정이 누락되었습니다.'));
  }

  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return Promise.resolve();
  }

  if (hasNaverMapSdk()) {
    return Promise.resolve();
  }

  const existingScript = document.getElementById(NAVER_MAP_SCRIPT_ID) as HTMLScriptElement | null;
  if (existingScript !== null && existingScript.dataset.clientId !== trimmedClientId) {
    existingScript.remove();
    naverMapScriptPromise = null;
  }

  if (naverMapScriptPromise !== null) {
    return naverMapScriptPromise;
  }

  const scriptElement = document.getElementById(NAVER_MAP_SCRIPT_ID) as HTMLScriptElement | null;
  const script = scriptElement ?? document.createElement('script');

  script.id = NAVER_MAP_SCRIPT_ID;
  script.async = true;
  script.src = buildNaverMapScriptUrl(trimmedClientId);
  script.dataset.clientId = trimmedClientId;

  naverMapScriptPromise = new Promise<void>((resolve, reject) => {
    script.onload = () => resolve();
    script.onerror = () => {
      naverMapScriptPromise = null;
      reject(new Error('네이버 지도 SDK를 불러오지 못했습니다.'));
    };
  });

  if (scriptElement === null) {
    document.head.appendChild(script);
  }

  return naverMapScriptPromise;
}

export function resetNaverMapScriptLoaderForTests(): void {
  naverMapScriptPromise = null;
  if (typeof document !== 'undefined') {
    document.getElementById(NAVER_MAP_SCRIPT_ID)?.remove();
  }
}
