import { useState, useCallback, type CSSProperties } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { ArrowLeft } from 'lucide-react';
import { ACTIVE_REGIONS, type RegionData } from '../../constants/regions';
import type { MapMode } from '../../constants/mapMode';

import { CountryMap } from '../CountryMap/CountryMap';
import { CityMap } from '../CityMap/CityMap';
import { DistrictMap } from '../DistrictMap/DistrictMap';

const ROOT_STYLE: CSSProperties = {
  position: 'relative',
  display: 'flex',
  width: '100%',
  minHeight: '100vh',
  overflow: 'hidden',
  background:
    'radial-gradient(circle at top, #f8fafc 0%, #eef2ff 48%, #e2e8f0 100%)',
  padding: 20,
};

const BACK_CONTROL_STYLE: CSSProperties = {
  position: 'absolute',
  top: 40,
  left: 40,
  zIndex: 20,
  display: 'flex',
  alignItems: 'center',
  gap: 12,
};

const BACK_BUTTON_STYLE: CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 8,
  padding: '10px 16px',
  background: 'rgba(255,255,255,0.9)',
  border: '1px solid rgba(148,163,184,0.2)',
  borderRadius: 16,
  boxShadow: '0 8px 24px rgba(15,23,42,0.08)',
  color: '#4b5563',
  cursor: 'pointer',
  backdropFilter: 'blur(12px)',
};

const BREADCRUMB_STYLE: CSSProperties = {
  padding: '10px 16px',
  background: 'rgba(255,255,255,0.7)',
  borderRadius: 16,
  border: '1px solid rgba(148,163,184,0.16)',
  color: '#6b7280',
  backdropFilter: 'blur(12px)',
};

const STAGE_CARD_STYLE: CSSProperties = {
  position: 'absolute',
  top: 40,
  right: 40,
  zIndex: 20,
  width: 'min(360px, calc(100vw - 80px))',
  borderRadius: 24,
  border: '1px solid rgba(255,255,255,0.7)',
  background: 'rgba(255,255,255,0.88)',
  padding: 20,
  boxShadow: '0 24px 80px rgba(15,23,42,0.16)',
  backdropFilter: 'blur(16px)',
};

const MAP_FRAME_STYLE: CSSProperties = {
  position: 'relative',
  width: '100%',
  height: 'calc(100vh - 40px)',
  minHeight: '560px',
  overflow: 'hidden',
  borderRadius: 36,
  border: '1px solid rgba(255,255,255,0.7)',
  background: 'rgba(255,255,255,0.65)',
  boxShadow: '0 32px 96px rgba(15,23,42,0.12)',
  backdropFilter: 'blur(12px)',
};

const LAYER_STYLE: CSSProperties = {
  position: 'absolute',
  inset: 0,
  width: '100%',
  height: '100%',
};

type ViewState =
  | { level: 'country' }
  | { level: 'city'; region: string; data: RegionData }
  | { level: 'district'; region: string; data: RegionData; guCode: string; guName: string; guCenter: [number, number] };

interface KoreaMapPropertySelection {
  propertyId: string;
  propertyName: string;
  propertyPrice: number;
  regionCode: string;
  districtCode: string;
}

interface KoreaMapProps {
  sessionId?: number;
  mode?: MapMode;
  onPropertySelected?: (selection: KoreaMapPropertySelection) => void;
  onLoanRequest?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}

function resolveInitialView(): ViewState {
  if (typeof window === 'undefined') {
    return { level: 'country' };
  }

  const params = new URLSearchParams(window.location.search);
  const regionCode = params.get('e2eRegionCode')?.trim();

  if (regionCode === undefined || regionCode.length === 0) {
    return { level: 'country' };
  }

  const regionEntry = Object.entries(ACTIVE_REGIONS).find(([, data]) => data.code === regionCode);
  if (regionEntry === undefined) {
    return { level: 'country' };
  }

  const [region, data] = regionEntry;
  const guCode = params.get('e2eDistrictCode')?.trim();
  const guName = params.get('e2eDistrictName')?.trim();
  const guCenterLng = Number(params.get('e2eCenterLng'));
  const guCenterLat = Number(params.get('e2eCenterLat'));

  if (
    guCode !== undefined &&
    guCode.length > 0 &&
    guName !== undefined &&
    guName.length > 0 &&
    Number.isFinite(guCenterLng) &&
    Number.isFinite(guCenterLat)
  ) {
    return {
      level: 'district',
      region,
      data,
      guCode,
      guName,
      guCenter: [guCenterLng, guCenterLat],
    };
  }

  return {
    level: 'city',
    region,
    data,
  };
}

export function KoreaMap({
  sessionId,
  mode = 'browse',
  onPropertySelected,
  onLoanRequest,
}: KoreaMapProps) {
  const [view, setView] = useState<ViewState>(() => resolveInitialView());

  const handleRegionClick = useCallback((region: string, data: RegionData) => {
    setView({ level: 'city', region, data });
  }, []);

  const handleGuClick = useCallback(
    (guCode: string, guName: string, guCenter: [number, number]) => {
      if (view.level === 'city') {
        setView({ level: 'district', region: view.region, data: view.data, guCode, guName, guCenter });
      }
    },
    [view],
  );

  const handleBack = useCallback(() => {
    if (view.level === 'district') {
      setView({ level: 'city', region: view.region, data: view.data });
    } else {
      setView({ level: 'country' });
    }
  }, [view]);

  const breadcrumb =
    view.level === 'country' ? '대한민국' : `대한민국 › ${view.data.label}`;
  const stageTitle =
    view.level === 'country'
      ? '지역을 먼저 고르세요'
      : view.level === 'city'
        ? `${view.data.label}에서 구를 선택하세요`
        : `${view.guName} 매물 지도를 보고 있어요`;
  const stageDescription =
    view.level === 'country'
      ? '시작할 광역시·도를 고르면 실제 매물 지도까지 자연스럽게 좁혀집니다.'
      : view.level === 'city'
        ? '구를 누르면 네이버 지도 위에 실제 매물 마커가 표시됩니다.'
        : '마커를 눌러 상세 정보를 보고, 왼쪽 패널이나 액션 버튼으로 다음 행동을 선택하세요.';

  return (
    <div style={ROOT_STYLE}>
      <AnimatePresence>
        {view.level !== 'country' && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.25 }}
            style={BACK_CONTROL_STYLE}
          >
            <button
              onClick={handleBack}
              style={BACK_BUTTON_STYLE}
            >
              <ArrowLeft size={18} color="#4b5563" />
              <span style={{ fontSize: 14, color: '#4b5563' }}>뒤로</span>
            </button>
            <div style={BREADCRUMB_STYLE}>
              <span style={{ fontSize: 13 }}>{breadcrumb}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
        style={STAGE_CARD_STYLE}
      >
        <div
          style={{
            marginBottom: 8,
            fontSize: 12,
            fontWeight: 700,
            letterSpacing: '0.24em',
            textTransform: 'uppercase',
            color: '#6366f1',
          }}
        >
          Real Estate Map
        </div>
        <div style={{ fontSize: 22, fontWeight: 900, lineHeight: 1.2, color: '#0f172a' }}>
          {stageTitle}
        </div>
        <p style={{ marginTop: 12, fontSize: 14, lineHeight: 1.7, color: '#475569' }}>
          {stageDescription}
        </p>
        <div style={{ marginTop: 16, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
          <span
            style={{
              borderRadius: 9999,
              background: '#f1f5f9',
              padding: '4px 12px',
              fontSize: 12,
              fontWeight: 600,
              color: '#475569',
            }}
          >
            {breadcrumb}
          </span>
          <span
            style={{
              borderRadius: 9999,
              background: '#eef2ff',
              padding: '4px 12px',
              fontSize: 12,
              fontWeight: 600,
              color: '#4f46e5',
            }}
          >
            {view.level === 'district' ? '실시간 매물 단계' : '지역 탐색 단계'}
          </span>
        </div>
      </motion.div>

      <div style={MAP_FRAME_STYLE}>
        <AnimatePresence mode="wait">
          {view.level === 'country' && (
            <motion.div
              key="country"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
              style={LAYER_STYLE}
            >
              <CountryMap onRegionClick={handleRegionClick} />
            </motion.div>
          )}

          {view.level === 'city' && (
            <motion.div
              key={`city-${view.region}`}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
              style={LAYER_STYLE}
            >
              <CityMap region={view.region} data={view.data} onGuClick={handleGuClick} />
            </motion.div>
          )}

          {view.level === 'district' && (
            <motion.div
              key={`district-${view.guCode}`}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
              style={LAYER_STYLE}
            >
              <DistrictMap
                regionCode={view.data.code}
                guCode={view.guCode}
                guCenter={view.guCenter}
                guName={view.guName}
                sessionId={sessionId}
                mode={mode}
                onPropertySelected={onPropertySelected}
                onLoanRequest={onLoanRequest}
              />
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
