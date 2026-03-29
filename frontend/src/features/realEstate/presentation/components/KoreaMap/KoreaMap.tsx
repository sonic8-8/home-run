import { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { ArrowLeft } from 'lucide-react';
import { ACTIVE_REGIONS, type RegionData } from '../../constants/regions';
import type { MapMode } from '../../pages/RealEstatePage/RealEstatePage';

import { CountryMap } from '../CountryMap/CountryMap';
import { CityMap } from '../CityMap/CityMap';
import { DistrictMap } from '../DistrictMap/DistrictMap';

type ViewState =
  | { level: 'country' }
  | { level: 'city'; region: string; data: RegionData }
  | { level: 'district'; region: string; data: RegionData; guCode: string; guName: string; guCenter: [number, number] };

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
}: {
  sessionId?: number;
  mode?: MapMode;
  onPropertySelected?: (selection: {
    propertyId: string;
    propertyName: string;
    propertyPrice: number;
    regionCode: string;
    districtCode: string;
  }) => void;
  onLoanRequest?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}) {
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
    <div className="relative flex min-h-screen size-full overflow-hidden bg-[radial-gradient(circle_at_top,#f8fafc_0%,#eef2ff_48%,#e2e8f0_100%)] p-5 md:p-6">
      <AnimatePresence>
        {view.level !== 'country' && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.25 }}
            className="absolute left-10 top-10 z-20 flex items-center gap-3"
          >
            <button
              onClick={handleBack}
              className="flex items-center gap-2 px-4 py-2.5 bg-white/90 backdrop-blur-sm rounded-xl shadow-sm hover:shadow-md hover:bg-white transition-all duration-200 cursor-pointer"
            >
              <ArrowLeft size={18} className="text-gray-600" />
              <span className="text-gray-600" style={{ fontSize: 14 }}>뒤로</span>
            </button>
            <div className="px-4 py-2.5 bg-white/70 backdrop-blur-sm rounded-xl">
              <span className="text-gray-500" style={{ fontSize: 13 }}>{breadcrumb}</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
        className="absolute right-10 top-10 z-20 w-[min(360px,calc(100vw-80px))] rounded-[24px] border border-white/70 bg-white/88 p-5 shadow-[0_24px_80px_rgba(15,23,42,0.16)] backdrop-blur"
      >
        <div className="mb-2 text-xs font-semibold uppercase tracking-[0.24em] text-indigo-500">
          Real Estate Map
        </div>
        <div className="text-[22px] font-black leading-tight text-slate-900">{stageTitle}</div>
        <p className="mt-3 text-sm leading-6 text-slate-600">{stageDescription}</p>
        <div className="mt-4 flex flex-wrap gap-2">
          <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">
            {breadcrumb}
          </span>
          <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-600">
            {view.level === 'district' ? '실시간 매물 단계' : '지역 탐색 단계'}
          </span>
        </div>
      </motion.div>

      <div className="relative h-[calc(100vh-40px)] w-full overflow-hidden rounded-[36px] border border-white/70 bg-white/65 shadow-[0_32px_96px_rgba(15,23,42,0.12)] backdrop-blur-sm">
        <AnimatePresence mode="wait">
          {view.level === 'country' && (
            <motion.div
              key="country"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
              className="absolute inset-0 h-full"
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
              className="absolute inset-0 h-full"
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
              className="absolute inset-0 h-full"
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
