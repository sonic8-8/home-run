import { useRef, useEffect, useState } from 'react';
import { loadNaverMapScript } from '../../utils/naverMapScript';

const NAVER_MAP_CLIENT_ID = import.meta.env.VITE_NAVER_MAP_CLIENT_ID?.trim() ?? '';

/**
 * 네이버 지도 초기화 커스텀 훅.
 * center: [lng, lat] 순서.
 * naverAvailable: null=확인중 / true=정상 / false=사용불가(폴백)
 */
export function useNaverMap(center: [number, number]) {
  const mapRef = useRef<HTMLDivElement>(null);
  const [mapInstance, setMapInstance] = useState<NaverMapInstance | null>(null);
  const centerLng = center[0];
  const centerLat = center[1];
  const [sdkReady, setSdkReady] = useState(() => {
    if (typeof window === 'undefined') return false;
    return window.naver?.maps !== undefined;
  });

  // localhost는 Naver Maps 도메인 인증 불가 → 즉시 폴백
  // 프로덕션 도메인에서 SDK 없으면 → 즉시 폴백
  const [naverAvailable, setNaverAvailable] = useState<boolean | null>(() => {
    if (typeof window === 'undefined') return false;
    const isLocalhost =
      window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    if (isLocalhost || NAVER_MAP_CLIENT_ID.length === 0) return false;
    return null;
  });
  const isNaverUnavailable = naverAvailable === false;

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    const isLocalhost =
      window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

    if (isLocalhost || NAVER_MAP_CLIENT_ID.length === 0) {
      return;
    }

    if (window.naver?.maps !== undefined) {
      return;
    }

    let cancelled = false;

    loadNaverMapScript(NAVER_MAP_CLIENT_ID)
      .then(() => {
        if (!cancelled) {
          setSdkReady(true);
          setNaverAvailable(null);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setSdkReady(false);
          setNaverAvailable(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    // SDK 없음 → 이미 false, 스킵
    if (isNaverUnavailable) return;
    if (!sdkReady) return;
    const maps = window.naver?.maps;
    if (maps === undefined) return;
    if (!mapRef.current) return;

    let map: NaverMapInstance;
    try {
      const naverCenter = new maps.LatLng(centerLat, centerLng);
      map = new maps.Map(mapRef.current, {
        center: naverCenter,
        zoom: 14,
        zoomControl: true,
        zoomControlOptions: {
          position: maps.Position.TOP_RIGHT,
          style: maps.ZoomControlStyle.SMALL,
        },
        mapTypeControl: false,
        scaleControl: true,
        logoControl: true,
        logoControlOptions: {
          position: maps.Position.BOTTOM_LEFT,
        },
      });
    } catch {
      window.setTimeout(() => {
        setNaverAvailable(false);
      }, 0);
      return;
    }

    // 타일 로드 이벤트로 도메인 인증 성공 여부 확인
    // 인증 실패 시 tilesloaded가 오지 않으므로 1.5초 후 폴백
    let tilesLoaded = false;
    const tilesListener = maps.Event.addListener(map, 'tilesloaded', () => {
      if (tilesLoaded) return;
      tilesLoaded = true;
      setNaverAvailable(true);
      setMapInstance(map);
    });

    const fallbackTimer = setTimeout(() => {
      if (!tilesLoaded) {
        setNaverAvailable(false);
      }
    }, 1500);

    return () => {
      clearTimeout(fallbackTimer);
      maps.Event.removeListener(tilesListener);
      setMapInstance(null);
    };
  }, [centerLat, centerLng, isNaverUnavailable, sdkReady]);

  return { mapRef, mapInstance, naverAvailable };
}
