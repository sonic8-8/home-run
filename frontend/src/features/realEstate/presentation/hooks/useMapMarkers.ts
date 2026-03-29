/* eslint-disable @typescript-eslint/no-explicit-any */

import { useRef, useCallback } from 'react';
import type { PropertySummary } from '../../domain/entities/Property';
import { formatPriceWon } from '../utils/formatUtils';

function buildPropertyMarkerIcon(property: PropertySummary, isSelected: boolean): string {
  const priceLabel = formatPriceWon(property.recentPrice);
  const bgColor = isSelected ? '#f59e0b' : '#6366f1';
  const shadow = isSelected
    ? '0 0 12px rgba(251,191,36,0.6), 0 4px 12px rgba(0,0,0,0.3)'
    : '0 2px 8px rgba(0,0,0,0.25)';
  const scale = isSelected ? 'transform:scale(1.12);' : '';

  return `<div style="display:flex;flex-direction:column;align-items:center;cursor:pointer;${scale}">
    <div style="position:relative;background:${bgColor};color:#fff;padding:5px 10px 6px;
        border-radius:8px;font-family:'Pretendard','Apple SD Gothic Neo',sans-serif;
        box-shadow:${shadow};text-align:center;min-width:52px;">
        <div style="font-size:10px;font-weight:600;color:rgba(255,255,255,0.85);line-height:1;margin-bottom:2px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:90px;">${property.name}</div>
        <div style="font-size:13px;font-weight:800;color:#fff;line-height:1.3;white-space:nowrap;">${priceLabel}</div>
    </div>
    <div style="width:0;height:0;border-left:6px solid transparent;border-right:6px solid transparent;border-top:6px solid ${bgColor};margin-top:-1px;"></div>
  </div>`;
}

export function useMapMarkers(mapInstance: any) {
  const markersRef = useRef<any[]>([]);
  const selectedMarkerRef = useRef<{ marker: any; property: PropertySummary } | null>(null);

  const clearMarkers = useCallback(() => {
    markersRef.current.forEach((m) => m.setMap(null));
    markersRef.current = [];
  }, []);

  const renderPropertyMarkers = useCallback(
    (
      properties: PropertySummary[],
      selectedPropertyId: string | null,
      onPropertyClick: (property: PropertySummary) => void,
    ) => {
      const maps = window.naver?.maps;
      if (maps === undefined) {
        clearMarkers();
        return;
      }

      clearMarkers();

      properties.forEach((property) => {
        const pos = new maps.LatLng(property.latitude, property.longitude);
        const isSelected = selectedPropertyId === property.propertyId;

        const marker = new maps.Marker({
          position: pos,
          map: mapInstance,
          zIndex: isSelected ? 999 : 1,
          icon: {
            content: buildPropertyMarkerIcon(property, isSelected),
            anchor: new maps.Point(40, 60),
          },
        });

        maps.Event.addListener(marker, 'click', () => {
          if (selectedMarkerRef.current) {
            const prev = selectedMarkerRef.current;
            prev.marker.setZIndex(1);
            prev.marker.setIcon({
              content: buildPropertyMarkerIcon(prev.property, false),
              anchor: new maps.Point(40, 60),
            });
          }

          marker.setZIndex(999);
          marker.setIcon({
            content: buildPropertyMarkerIcon(property, true),
            anchor: new maps.Point(40, 60),
          });
          selectedMarkerRef.current = { marker, property };

          onPropertyClick(property);
        });

        markersRef.current.push(marker);
      });
    },
    [mapInstance, clearMarkers],
  );

  return { clearMarkers, renderPropertyMarkers };
}
