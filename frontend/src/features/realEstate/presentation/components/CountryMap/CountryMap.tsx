/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState } from 'react';
import { ComposableMap, Geographies, Geography, Marker } from 'react-simple-maps';
import type { RegionData } from '../../constants/regions';
import {
  GEO_PROVINCES,
  ACTIVE_REGIONS,
  LIFT_HEIGHT,
  TRANSITION,
  TRANSITION_FADE,
} from '../../constants/regions';
import { matchRegion, getName, getShortName, computeCentroid, getLabelOffset } from '../../utils/geoUtils';
import { WallLayers } from '../WallLayers/WallLayers';

export function CountryMap({ onRegionClick }: { onRegionClick: (region: string, data: RegionData) => void }) {
  const [hoveredKey, setHoveredKey] = useState<string | null>(null);

  return (
    <ComposableMap
      projection="geoMercator"
      projectionConfig={{ scale: 10000, center: [127.5, 35.8] }}
      width={1100}
      height={1200}
      style={{ width: '100%', height: '100%' }}
    >
      <Geographies geography={GEO_PROVINCES}>
        {({ geographies }: any) => {
          const activeGeos: { geo: any; keyword: string; data: RegionData }[] = [];
          geographies.forEach((geo: any) => {
            const kw = matchRegion(geo);
            if (kw) activeGeos.push({ geo, keyword: kw, data: ACTIVE_REGIONS[kw] });
          });

          return (
            <>
              {activeGeos.map(({ geo, keyword }) => {
                return (
                  <g
                    key={geo.rsmKey}
                    onMouseEnter={() => setHoveredKey(geo.rsmKey)}
                    onMouseLeave={() => setHoveredKey(null)}
                    onClick={() => { if (keyword) onRegionClick(keyword, ACTIVE_REGIONS[keyword]); }}
                    data-testid={`country-region-${ACTIVE_REGIONS[keyword].code}`}
                    style={{ cursor: 'pointer' }}
                  >
                    <Geography
                      geography={geo}
                      style={{
                        default: { fill: '#ffffff', stroke: '#d0d2d8', strokeWidth: 0.7, outline: 'none' },
                        hover:   { fill: '#ffffff', stroke: '#b8bac0', strokeWidth: 0.8, outline: 'none' },
                        pressed: { fill: '#ffffff', stroke: '#b8bac0', strokeWidth: 0.8, outline: 'none' },
                      }}
                    />
                  </g>
                );
              })}

              {activeGeos.map(({ geo, data }) => {
                const raised = geo.rsmKey === hoveredKey;
                const groundStyle = {
                  fill: raised ? '#d4d6dc' : '#ffffff',
                  stroke: raised ? '#b0b2ba' : '#d0d2d8',
                  strokeWidth: raised ? 1 : 0.7,
                  outline: 'none' as const,
                  transition: TRANSITION_FADE,
                };
                const topStyle = {
                  fill: raised ? data.color : '#ffffff',
                  fillOpacity: 1,
                  stroke: raised ? '#ffffff' : '#d0d2d8',
                  strokeWidth: raised ? 1.5 : 0.7,
                  outline: 'none' as const,
                  transform: `translateY(${raised ? -LIFT_HEIGHT : 0}px)`,
                  filter: raised ? 'drop-shadow(0 4px 6px rgba(0,0,0,0.15))' : 'drop-shadow(0 0px 0px rgba(0,0,0,0))',
                  transition: TRANSITION,
                };
                return (
                  <g key={`overlay-${geo.rsmKey}`} style={{ pointerEvents: 'none' }}>
                    <Geography geography={geo} style={{ default: groundStyle, hover: groundStyle, pressed: groundStyle }} />
                    <WallLayers geo={geo} sideColor={data.sideColor} raised={raised} />
                    <Geography geography={geo} style={{ default: topStyle, hover: topStyle, pressed: topStyle }} />
                  </g>
                );
              })}

              {activeGeos.map(({ geo }) => {
                const raised = geo.rsmKey === hoveredKey;
                const name = getShortName(getName(geo.properties));
                const center = computeCentroid(geo);
                const offset = getLabelOffset(name);
                return (
                  <Marker key={`label-${geo.rsmKey}`} coordinates={[center[0] + offset[0], center[1] + offset[1]]}>
                    <g style={{ pointerEvents: 'none', transform: `translateY(${raised ? -LIFT_HEIGHT : 0}px)`, transition: TRANSITION }}>
                      <text
                        textAnchor="middle"
                        dominantBaseline="central"
                        style={{
                          fontFamily: "'Pretendard', 'Apple SD Gothic Neo', sans-serif",
                          fontSize: 18,
                          fontWeight: 600,
                          fill: raised ? '#ffffff' : '#374151',
                          paintOrder: 'stroke',
                          stroke: raised ? 'rgba(59,130,246,0.3)' : 'rgba(255,255,255,0.8)',
                          strokeWidth: raised ? 3.5 : 3,
                          strokeLinecap: 'round' as const,
                          strokeLinejoin: 'round' as const,
                          transition: 'all 0.3s ease-out',
                        }}
                      >
                        {name}
                      </text>
                    </g>
                  </Marker>
                );
              })}
            </>
          );
        }}
      </Geographies>
    </ComposableMap>
  );
}
