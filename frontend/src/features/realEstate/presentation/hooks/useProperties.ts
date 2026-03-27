/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState, useEffect, useCallback } from 'react';
import { container } from '@core/di/container';
import type { PropertySummary, Property } from '../../domain/entities/Property';
import type { MapMode } from '../pages/RealEstatePage/RealEstatePage';
import { GetPropertiesUseCase } from '../../domain/usecases/GetPropertiesUseCase';
import { GetPropertyDetailUseCase } from '../../domain/usecases/GetPropertyDetailUseCase';

/**
 * 게임 부동산 매물 데이터 훅.
 * 백엔드 API에서 매물 목록을 가져와 지도 마커로 표시합니다.
 */
export function useProperties(
  _mapInstance: any,
  options: {
    mode?: MapMode;
    sessionId?: number;
    regionCode?: string;
    districtCode?: string;
  },
) {
  const {
    mode = 'browse',
    sessionId,
    regionCode,
    districtCode,
  } = options;
  const [properties, setProperties] = useState<PropertySummary[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [selectedProperty, setSelectedProperty] = useState<PropertySummary | null>(null);
  const [selectedPropertyDetail, setSelectedPropertyDetail] = useState<Property | null>(null);

  const hasQuery =
    mode === 'new-game'
      ? regionCode !== undefined && districtCode !== undefined
      : sessionId !== undefined;
  const loading = hasQuery && !loaded;

  useEffect(() => {
    if (!hasQuery) {
      return;
    }

    let cancelled = false;
    const getPropertiesUseCase = container.resolve(GetPropertiesUseCase);
    const query =
      mode === 'new-game'
        ? { regionCode, districtCode }
        : { sessionId };

    setLoaded(false);
    setSelectedProperty(null);
    setSelectedPropertyDetail(null);

    getPropertiesUseCase.execute(query)
      .then((list) => {
        if (!cancelled) {
          setProperties(list);
          setLoaded(true);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setLoaded(true);
        }
      });

    return () => { cancelled = true; };
  }, [districtCode, hasQuery, mode, regionCode, sessionId]);

  const selectProperty = useCallback(async (property: PropertySummary) => {
    setSelectedProperty(property);
    setSelectedPropertyDetail(null);

    if (mode === 'new-game' || sessionId === undefined) {
      return;
    }

    try {
      const getPropertyDetailUseCase = container.resolve(GetPropertyDetailUseCase);
      const detail = await getPropertyDetailUseCase.execute(sessionId, property.propertyId);
      setSelectedPropertyDetail(detail);
    } catch {
      // 상세 조회 실패 시 summary 데이터만 표시
    }
  }, [mode, sessionId]);

  const clearSelection = useCallback(() => {
    setSelectedProperty(null);
    setSelectedPropertyDetail(null);
  }, []);

  return {
    properties,
    selectedProperty,
    selectedPropertyDetail,
    loading,
    selectProperty,
    clearSelection,
  };
}
