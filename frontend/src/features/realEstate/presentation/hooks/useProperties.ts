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
  const [propertyState, setPropertyState] = useState<{
    requestKey: string | null;
    properties: PropertySummary[];
    loaded: boolean;
  }>({
    requestKey: null,
    properties: [],
    loaded: false,
  });
  const [selectionState, setSelectionState] = useState<{
    requestKey: string | null;
    selectedProperty: PropertySummary | null;
    selectedPropertyDetail: Property | null;
  }>({
    requestKey: null,
    selectedProperty: null,
    selectedPropertyDetail: null,
  });

  const hasQuery =
    mode === 'new-game'
      ? regionCode !== undefined && districtCode !== undefined
      : sessionId !== undefined;
  const requestKey = hasQuery
    ? mode === 'new-game'
      ? `${mode}:${regionCode}:${districtCode}`
      : `${mode}:${sessionId}`
    : null;
  const properties =
    requestKey !== null && propertyState.requestKey === requestKey
      ? propertyState.properties
      : [];
  const loading =
    requestKey !== null
      && (propertyState.requestKey !== requestKey || propertyState.loaded === false);
  const selectedProperty =
    requestKey !== null && selectionState.requestKey === requestKey
      ? selectionState.selectedProperty
      : null;
  const selectedPropertyDetail =
    requestKey !== null && selectionState.requestKey === requestKey
      ? selectionState.selectedPropertyDetail
      : null;

  useEffect(() => {
    if (!hasQuery || requestKey === null) {
      return;
    }

    let cancelled = false;
    const getPropertiesUseCase = container.resolve(GetPropertiesUseCase);
    const query =
      mode === 'new-game'
        ? { regionCode, districtCode }
        : { sessionId };

    getPropertiesUseCase.execute(query)
      .then((list) => {
        if (!cancelled) {
          setPropertyState({
            requestKey,
            properties: list,
            loaded: true,
          });
        }
      })
      .catch(() => {
        if (!cancelled) {
          setPropertyState({
            requestKey,
            properties: [],
            loaded: true,
          });
        }
      });

    return () => { cancelled = true; };
  }, [districtCode, hasQuery, mode, regionCode, requestKey, sessionId]);

  const selectProperty = useCallback(async (property: PropertySummary) => {
    if (requestKey === null) {
      return;
    }

    setSelectionState({
      requestKey,
      selectedProperty: property,
      selectedPropertyDetail: null,
    });

    if (mode === 'new-game' || sessionId === undefined) {
      return;
    }

    try {
      const getPropertyDetailUseCase = container.resolve(GetPropertyDetailUseCase);
      const detail = await getPropertyDetailUseCase.execute(sessionId, property.propertyId);

      setSelectionState((current) => {
        if (current.requestKey !== requestKey || current.selectedProperty?.propertyId !== property.propertyId) {
          return current;
        }

        return {
          requestKey,
          selectedProperty: property,
          selectedPropertyDetail: detail,
        };
      });
    } catch {
      // 상세 조회 실패 시 summary 데이터만 표시
    }
  }, [mode, requestKey, sessionId]);

  const clearSelection = useCallback(() => {
    setSelectionState((current) => ({
      requestKey: requestKey ?? current.requestKey,
      selectedProperty: null,
      selectedPropertyDetail: null,
    }));
  }, [requestKey]);

  return {
    properties,
    selectedProperty,
    selectedPropertyDetail,
    loading,
    selectProperty,
    clearSelection,
  };
}
