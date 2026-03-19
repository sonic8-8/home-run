/* eslint-disable @typescript-eslint/no-explicit-any */

import { Geography } from 'react-simple-maps';
import { WALL_LAYERS, LIFT_HEIGHT, TRANSITION } from '../../constants/regions';

export function WallLayers({ geo, sideColor, raised }: { geo: any; sideColor: string; raised: boolean }) {
  const layers = [];
  for (let i = 0; i < WALL_LAYERS; i++) {
    const targetY = raised ? -(i / WALL_LAYERS) * LIFT_HEIGHT : 0;
    const darkness = 1 - i / WALL_LAYERS;
    const style = {
      fill: sideColor,
      fillOpacity: raised ? 0.5 + darkness * 0.3 : 0,
      stroke: sideColor,
      strokeWidth: 0.4,
      strokeOpacity: raised ? 0.4 : 0,
      outline: 'none' as const,
      transform: `translateY(${targetY}px)`,
      transition: TRANSITION,
    };
    layers.push(
      <Geography key={`w${i}`} geography={geo} style={{ default: style, hover: style, pressed: style }} />,
    );
  }
  return <>{layers}</>;
}
