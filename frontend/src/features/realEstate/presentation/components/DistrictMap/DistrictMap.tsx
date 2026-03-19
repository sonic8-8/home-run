import { useEffect } from 'react';
import { useNaverMap } from '../../hooks/useNaverMap';
import { useProperties } from '../../hooks/useProperties';
import { useMapMarkers } from '../../hooks/useMapMarkers';
import { PropertyDetailPanel } from '../PropertyDetailPanel/PropertyDetailPanel';
import { formatPriceWon } from '../../utils/formatUtils';
import type { MapMode } from '../../pages/RealEstatePage/RealEstatePage';

/**
 * 동 레벨 지도 컴포넌트 (게임용).
 * 네이버 지도 위에 게임 매물 마커를 표시합니다.
 * 네이버 SDK 미로드 시 카드 목록 폴백으로 동일한 흐름을 진행할 수 있습니다.
 */
export function DistrictMap({
  guCenter,
  guName,
  sessionId,
  mode = 'browse',
  onPropertySelected,
  onLoanRequest,
}: {
  guCode: string;
  guCenter: [number, number];
  guName?: string;
  sessionId: number;
  mode?: MapMode;
  onPropertySelected?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
  onLoanRequest?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}) {
  const { mapRef, mapInstance, naverAvailable } = useNaverMap(guCenter);
  const { properties, selectedProperty, selectedPropertyDetail, selectProperty, clearSelection } =
    useProperties(mapInstance, sessionId);
  const { renderPropertyMarkers } = useMapMarkers(mapInstance);

  useEffect(() => {
    if (!mapInstance || properties.length === 0) return;
    renderPropertyMarkers(properties, selectedProperty?.propertyId ?? null, selectProperty);
  }, [mapInstance, properties, selectedProperty, renderPropertyMarkers, selectProperty]);

  // 네이버 지도 SDK 미로드 시 카드 목록 폴백
  if (naverAvailable === false) {
    return (
      <div
        style={{
          width: '100vw',
          height: '100vh',
          background: '#eceef2',
          display: 'flex',
          overflow: 'hidden',
          fontFamily: "'Pretendard', 'Apple SD Gothic Neo', sans-serif",
        }}
      >
        {/* 좌측: 상세 패널 */}
        {selectedProperty && (
          <div style={{ width: 360, flexShrink: 0, height: '100%', position: 'relative' }}>
            <PropertyDetailPanel
              summary={selectedProperty}
              detail={selectedPropertyDetail}
              onClose={clearSelection}
              mode={mode}
              onSelect={onPropertySelected}
              onLoanRequest={onLoanRequest}
            />
          </div>
        )}

        {/* 우측: 매물 카드 목록 */}
        <div
          style={{
            flex: 1,
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 16,
            padding: 40,
            overflowY: 'auto',
          }}
        >
          <div style={{ fontSize: 16, fontWeight: 700, color: '#374151', marginBottom: 4 }}>
            {guName ?? '선택한 지역'} 매물 목록
          </div>
          {properties.map((p) => (
            <button
              key={p.propertyId}
              onClick={() => selectProperty(p)}
              style={{
                width: '100%',
                maxWidth: 520,
                padding: '20px 28px',
                background: selectedProperty?.propertyId === p.propertyId ? '#6366f1' : '#fff',
                border: selectedProperty?.propertyId === p.propertyId ? '2px solid #4f46e5' : '1px solid #e5e7eb',
                borderRadius: 16,
                boxShadow: selectedProperty?.propertyId === p.propertyId
                  ? '0 4px 20px rgba(99,102,241,0.3)'
                  : '0 2px 8px rgba(0,0,0,0.08)',
                cursor: 'pointer',
                textAlign: 'left',
                fontFamily: 'inherit',
                transition: 'all 0.2s',
              }}
            >
              <div style={{ fontSize: 15, fontWeight: 700, color: selectedProperty?.propertyId === p.propertyId ? '#fff' : '#111827', marginBottom: 6 }}>
                {p.name}
              </div>
              <div style={{ fontSize: 22, fontWeight: 800, color: selectedProperty?.propertyId === p.propertyId ? '#e0e7ff' : '#6366f1' }}>
                {formatPriceWon(p.recentPrice)}
              </div>
            </button>
          ))}
          {properties.length === 0 && (
            <div style={{ color: '#9ca3af', fontSize: 14 }}>매물을 불러오는 중...</div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div style={{ position: 'relative', width: '100%', height: '100%' }}>
      <div ref={mapRef} style={{ width: '100%', height: '100%', overflow: 'hidden' }} />

      {selectedProperty && (
        <PropertyDetailPanel
          summary={selectedProperty}
          detail={selectedPropertyDetail}
          onClose={clearSelection}
          mode={mode}
          onSelect={onPropertySelected}
          onLoanRequest={onLoanRequest}
        />
      )}
    </div>
  );
}
