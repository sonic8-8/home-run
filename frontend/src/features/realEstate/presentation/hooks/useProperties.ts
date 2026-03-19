/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState, useEffect, useCallback } from 'react';
import type { PropertySummary, Property } from '../../domain/entities/Property';
import { RealEstateRemoteDataSource } from '../../data/datasources/RealEstateRemoteDataSource';
import { RealEstateRepositoryImpl } from '../../data/repositories/RealEstateRepositoryImpl';
import { GetPropertiesUseCase } from '../../domain/usecases/GetPropertiesUseCase';
import { GetPropertyDetailUseCase } from '../../domain/usecases/GetPropertyDetailUseCase';

const dataSource = new RealEstateRemoteDataSource();
const repository = new RealEstateRepositoryImpl(dataSource);
const getPropertiesUseCase = new GetPropertiesUseCase(repository);
const getPropertyDetailUseCase = new GetPropertyDetailUseCase(repository);

/**
 * 게임 부동산 매물 데이터 훅.
 * 백엔드 API에서 매물 목록을 가져와 지도 마커로 표시합니다.
 */
export function useProperties(mapInstance: any, sessionId: number) {
  const [properties, setProperties] = useState<PropertySummary[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [selectedProperty, setSelectedProperty] = useState<PropertySummary | null>(null);
  const [selectedPropertyDetail, setSelectedPropertyDetail] = useState<Property | null>(null);

  const loading = !!sessionId && !loaded;

  useEffect(() => {
    if (!sessionId) return;

    let cancelled = false;

    // TODO: bounds를 지도 viewport에서 동적으로 계산
    getPropertiesUseCase.execute(sessionId, '').then((list) => {
      if (!cancelled) {
        setProperties(list);
        setLoaded(true);
      }
    }).catch(() => {
      if (!cancelled) setLoaded(true);
    });

    return () => { cancelled = true; };
  }, [sessionId]);

  const selectProperty = useCallback(async (property: PropertySummary) => {
    setSelectedProperty(property);
    setSelectedPropertyDetail(null);

    try {
      const detail = await getPropertyDetailUseCase.execute(sessionId, property.propertyId);
      setSelectedPropertyDetail(detail);
    } catch {
      // 상세 조회 실패 시 summary 데이터만 표시
    }
  }, [sessionId]);

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
