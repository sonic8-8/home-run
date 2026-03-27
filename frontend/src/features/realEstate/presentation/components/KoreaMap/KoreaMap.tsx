import { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { ArrowLeft } from 'lucide-react';
import type { RegionData } from '../../constants/regions';
import type { MapMode } from '../../pages/RealEstatePage/RealEstatePage';

import { CountryMap } from '../CountryMap/CountryMap';
import { CityMap } from '../CityMap/CityMap';
import { DistrictMap } from '../DistrictMap/DistrictMap';

type ViewState =
  | { level: 'country' }
  | { level: 'city'; region: string; data: RegionData }
  | { level: 'district'; region: string; data: RegionData; guCode: string; guName: string; guCenter: [number, number] };

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
  const [view, setView] = useState<ViewState>({ level: 'country' });

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

  return (
    <div className="size-full min-h-screen bg-[#eceef2] flex overflow-hidden relative">
      <AnimatePresence>
        {view.level !== 'country' && (
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.25 }}
            className="absolute top-6 left-6 z-10 flex items-center gap-3"
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

      <div className="relative w-full h-screen">
        <AnimatePresence mode="wait">
          {view.level === 'country' && (
            <motion.div
              key="country"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 1.1 }}
              transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
              className="absolute inset-0"
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
              className="absolute inset-0"
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
              className="absolute inset-0"
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
