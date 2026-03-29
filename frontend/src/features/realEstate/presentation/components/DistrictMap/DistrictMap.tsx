import { useEffect, useCallback } from 'react';
import { useNaverMap } from '../../hooks/useNaverMap';
import { useProperties } from '../../hooks/useProperties';
import { useMapMarkers } from '../../hooks/useMapMarkers';
import { useRegistryReview } from '../../hooks/useRegistryReview';
import type { PropertySummary } from '../../../domain/entities/Property';
import { PropertyDetailPanel } from '../PropertyDetailPanel/PropertyDetailPanel';
import { RegistryDocumentModal } from '../RegistryDocumentModal/RegistryDocumentModal';
import type { MapMode } from '../../constants/mapMode';
import { formatPriceWon } from '../../utils/formatUtils';

/**
 * 동 레벨 지도 컴포넌트 (게임용).
 * 네이버 지도 위에 게임 매물 마커를 표시합니다.
 * 네이버 SDK 미로드 시 카드 목록 폴백으로 동일한 흐름을 진행할 수 있습니다.
 */
interface DistrictMapPropertySelection {
  propertyId: string;
  propertyName: string;
  propertyPrice: number;
  regionCode: string;
  districtCode: string;
}

interface DistrictMapProps {
  regionCode: string;
  guCode: string;
  guCenter: [number, number];
  guName?: string;
  sessionId?: number;
  mode?: MapMode;
  onPropertySelected?: (selection: DistrictMapPropertySelection) => void;
  onLoanRequest?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}

export function DistrictMap({
  regionCode,
  guCode,
  guCenter,
  guName,
  sessionId,
  mode = 'browse',
  onPropertySelected,
  onLoanRequest,
}: DistrictMapProps) {
  const { mapRef, mapInstance, naverAvailable } = useNaverMap(guCenter);
  const {
    properties,
    selectedProperty,
    selectedPropertyDetail,
    loading,
    selectProperty,
    clearSelection,
  } =
    useProperties({
      mode,
      sessionId,
      regionCode,
      districtCode: guCode,
    });
  const { renderPropertyMarkers } = useMapMarkers(mapInstance);
  const {
    registryDoc,
    isOpen: registryModalOpen,
    isSubmitting: isRegistrySubmitting,
    loadError,
    selectedTrapIds,
    submitError,
    reviewResult,
    openReview,
    closeReview,
    toggleTrap,
    resetSelection,
    submitReview,
  } = useRegistryReview(sessionId);

  const handlePropertySelect = useCallback((property: PropertySummary) => {
    closeReview();
    selectProperty(property);
  }, [closeReview, selectProperty]);

  const handlePanelClose = useCallback(() => {
    closeReview();
    clearSelection();
  }, [clearSelection, closeReview]);

  useEffect(() => {
    if (!mapInstance || properties.length === 0) return;
    renderPropertyMarkers(properties, selectedProperty?.propertyId ?? null, handlePropertySelect);
  }, [handlePropertySelect, mapInstance, properties, renderPropertyMarkers, selectedProperty]);

  const handleBrowsePurchase = useCallback(async (propertyId: string) => {
    await openReview(propertyId);
  }, [openReview]);

  const propertyDetailProps = {
    mode,
    onSelect:
      mode === 'new-game' && onPropertySelected !== undefined
        ? (propertyId: string, propertyName: string, propertyPrice: number) =>
            onPropertySelected({
              propertyId,
              propertyName,
              propertyPrice,
              regionCode,
              districtCode: guCode,
            })
        : undefined,
    onLoanRequest,
    onBrowsePurchase: mode === 'browse' ? handleBrowsePurchase : undefined,
    browsePurchaseError: mode === 'browse' ? loadError : null,
  };

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
              onClose={handlePanelClose}
              {...propertyDetailProps}
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
          <div
            style={{
              maxWidth: 520,
              width: '100%',
              borderRadius: 20,
              background: 'rgba(255,255,255,0.86)',
              border: '1px solid rgba(99,102,241,0.14)',
              boxShadow: '0 18px 40px rgba(15,23,42,0.08)',
              padding: '18px 20px',
            }}
          >
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.16em', textTransform: 'uppercase', color: '#6366f1' }}>
              Map Fallback
            </div>
            <div style={{ fontSize: 18, fontWeight: 800, color: '#111827', marginTop: 6 }}>
              {guName ?? '선택한 지역'} 매물 목록
            </div>
            <div style={{ fontSize: 13, lineHeight: 1.6, color: '#6b7280', marginTop: 8 }}>
              지도를 사용할 수 없는 환경이라 카드형 목록으로 대신 보여주고 있습니다. 항목을 누르면 상세 패널과 다음 행동이 이어집니다.
            </div>
          </div>
          <div style={{ fontSize: 16, fontWeight: 700, color: '#374151', marginBottom: 4 }}>
            {guName ?? '선택한 지역'} 매물 목록
          </div>
          {properties.map((p) => (
            <button
              key={p.propertyId}
              onClick={() => handlePropertySelect(p)}
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

        {registryDoc && (
          <RegistryDocumentModal
            isOpen={registryModalOpen}
            onClose={closeReview}
            doc={registryDoc}
            selectedTrapIds={selectedTrapIds}
            isSubmitting={isRegistrySubmitting}
            submitError={submitError}
            reviewResult={reviewResult}
            onToggleTrap={toggleTrap}
            onResetSelection={resetSelection}
            onSubmitReview={submitReview}
          />
        )}
      </div>
    );
  }

  return (
    <div
      style={{
        position: 'relative',
        width: '100%',
        height: '100%',
        minHeight: 'max(560px, calc(100vh - 40px))',
      }}
    >
      <div
        ref={mapRef}
        data-testid="naver-live-map"
        data-map-ready={naverAvailable === true ? 'true' : 'false'}
        style={{ width: '100%', height: '100%', minHeight: '100%', overflow: 'hidden' }}
      />
      <div
        style={{
          position: 'absolute',
          top: 24,
          right: 24,
          zIndex: 20,
          width: 320,
          borderRadius: 22,
          background: 'rgba(255,255,255,0.9)',
          border: '1px solid rgba(99,102,241,0.12)',
          boxShadow: '0 20px 48px rgba(15,23,42,0.14)',
          backdropFilter: 'blur(16px)',
          padding: '18px 18px 16px',
        }}
      >
        <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.16em', textTransform: 'uppercase', color: '#6366f1' }}>
          Live Map
        </div>
        <div style={{ marginTop: 6, fontSize: 18, fontWeight: 800, color: '#111827' }}>
          {guName ?? '선택한 지역'} 부동산 지도
        </div>
        <div style={{ marginTop: 8, fontSize: 13, lineHeight: 1.6, color: '#6b7280' }}>
          {loading
            ? '매물과 마커를 불러오는 중입니다. 잠시만 기다려주세요.'
            : properties.length === 0
              ? '이 구역에는 아직 노출 가능한 매물이 없습니다. 다른 구를 선택해보세요.'
              : '마커를 누르면 왼쪽 상세 패널에서 가격과 액션 버튼을 바로 확인할 수 있습니다.'}
        </div>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 14 }}>
          <span style={{ borderRadius: 9999, background: '#eef2ff', color: '#4338ca', padding: '7px 12px', fontSize: 12, fontWeight: 600 }}>
            파란 마커: 기본 매물
          </span>
          <span style={{ borderRadius: 9999, background: '#fffbeb', color: '#b45309', padding: '7px 12px', fontSize: 12, fontWeight: 600 }}>
            노란 마커: 선택한 매물
          </span>
        </div>
      </div>

      {selectedProperty && (
        <PropertyDetailPanel
          summary={selectedProperty}
          detail={selectedPropertyDetail}
          onClose={handlePanelClose}
          {...propertyDetailProps}
        />
      )}

      {registryDoc && (
        <RegistryDocumentModal
          isOpen={registryModalOpen}
          onClose={closeReview}
          doc={registryDoc}
          selectedTrapIds={selectedTrapIds}
          isSubmitting={isRegistrySubmitting}
          submitError={submitError}
          reviewResult={reviewResult}
          onToggleTrap={toggleTrap}
          onResetSelection={resetSelection}
          onSubmitReview={submitReview}
        />
      )}
    </div>
  );
}
