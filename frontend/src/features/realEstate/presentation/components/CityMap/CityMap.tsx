/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState } from 'react';
import { ComposableMap, Geographies, Geography, Marker } from 'react-simple-maps';
import type { RegionData } from '../../constants/regions';
import { GEO_MUNICIPALITIES, LIFT_HEIGHT, TRANSITION, TRANSITION_FADE } from '../../constants/regions';
import { matchByCode, getName, getCode, computeCentroid, getGuLabelOffset } from '../../utils/geoUtils';
import { WallLayers } from '../WallLayers/WallLayers';

export function CityMap({
  data,
  onGuClick,
}: {
  region: string;
  data: RegionData;
  onGuClick: (guCode: string, guName: string, guCenter: [number, number]) => void;
}) {
  const [hoveredKey, setHoveredKey] = useState<string | null>(null);

  function computeCenter(geo: any): [number, number] {
    const coords = geo.geometry?.coordinates;
    let cLng = data.center[0];
    let cLat = data.center[1];
    if (coords) {
      const flat = coords.flat(Infinity);
      let sumLng = 0, sumLat = 0, count = 0;
      for (let i = 0; i < flat.length; i += 2) {
        if (typeof flat[i] === 'number' && typeof flat[i + 1] === 'number') {
          sumLng += flat[i];
          sumLat += flat[i + 1];
          count++;
        }
      }
      if (count > 0) { cLng = sumLng / count; cLat = sumLat / count; }
    }
    return [cLng, cLat];
  }

  const baseStyle = { fill: '#ffffff', stroke: '#d0d2d8', strokeWidth: 0.7, outline: 'none' };

  return (
    <ComposableMap
      projection="geoMercator"
      projectionConfig={{ scale: data.scale, center: data.center }}
      width={1100}
      height={1200}
      style={{ width: '100%', height: '100%' }}
    >
      <Geographies geography={GEO_MUNICIPALITIES}>
        {({ geographies }) => {
          const filtered = geographies.filter((geo) => matchByCode(geo, data.code));
          return (
            <>
              {filtered.map((geo) => {
                const name = getName(geo.properties);
                const code = getCode(geo.properties);
                return (
                  <g
                    key={geo.rsmKey}
                    onMouseEnter={() => setHoveredKey(geo.rsmKey)}
                    onMouseLeave={() => setHoveredKey(null)}
                    onClick={() => onGuClick(code, name, computeCenter(geo))}
                    style={{ cursor: 'pointer' }}
                  >
                    <Geography geography={geo} style={{ default: baseStyle, hover: baseStyle, pressed: baseStyle }} />
                  </g>
                );
              })}

              {[...filtered]
                .sort((a, b) => {
                  if (a.rsmKey === hoveredKey) return 1;
                  if (b.rsmKey === hoveredKey) return -1;
                  return 0;
                })
                .map((geo) => {
                  const raised = geo.rsmKey === hoveredKey;
                  const groundStyle = {
                    fill: raised ? '#d4d6dc' : '#f0f1f5',
                    stroke: raised ? '#b0b2ba' : '#d0d2d8',
                    strokeWidth: raised ? 1 : 0.7,
                    outline: 'none' as const,
                    transition: TRANSITION_FADE,
                  };
                  const topStyle = {
                    fill: raised ? data.color : '#f0f1f5',
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

              {filtered.map((geo) => {
                const raised = geo.rsmKey === hoveredKey;
                const name = getName(geo.properties);
                const center = computeCentroid(geo, data.center);
                const offset = getGuLabelOffset(name);
                return (
                  <Marker key={`label-gu-${geo.rsmKey}`} coordinates={[center[0] + offset[0], center[1] + offset[1]]}>
                    <g style={{ pointerEvents: 'none', transform: `translateY(${raised ? -LIFT_HEIGHT : 0}px)`, transition: TRANSITION }}>
                      <text
                        textAnchor="middle"
                        dominantBaseline="central"
                        style={{
                          fontFamily: "'Pretendard', 'Apple SD Gothic Neo', sans-serif",
                          fontSize: raised ? 16 : 13,
                          fontWeight: raised ? 700 : 500,
                          fill: raised ? '#ffffff' : '#4b5563',
                          paintOrder: 'stroke',
                          stroke: raised ? 'rgba(59,130,246,0.3)' : 'rgba(255,255,255,0.85)',
                          strokeWidth: raised ? 3 : 2.5,
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
