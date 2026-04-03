import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CountryMap } from './CountryMap';

const mockState = vi.hoisted(() => ({
  lastComposableMapProps: null as null | {
    projection?: string;
    projectionConfig?: {
      scale?: number;
      center?: [number, number];
    };
  },
}));

vi.mock('react-simple-maps', () => ({
  ComposableMap: ({
    children,
    ...props
  }: {
    children: React.ReactNode;
    projection?: string;
    projectionConfig?: {
      scale?: number;
      center?: [number, number];
    };
  }) => {
    mockState.lastComposableMapProps = props;
    return <div>{children}</div>;
  },
  Geographies: ({
    children,
  }: {
    geography: string;
    children: (args: { geographies: Array<Record<string, unknown>> }) => React.ReactNode;
  }) =>
    children({
      geographies: [
        { rsmKey: 'seoul', properties: { name: '서울특별시', name_eng: 'Seoul', code: '11' }, geometry: { coordinates: [[[126.9, 37.5]]] } },
        { rsmKey: 'gwangju', properties: { name: '광주광역시', name_eng: 'Gwangju', code: '24' }, geometry: { coordinates: [[[126.8, 35.1]]] } },
        { rsmKey: 'sejong', properties: { name: '세종특별자치시', name_eng: 'Sejongsi', code: '29' }, geometry: { coordinates: [[[127.2, 36.5]]] } },
        { rsmKey: 'busan', properties: { name: '부산광역시' }, geometry: { coordinates: [[[129.0, 35.1]]] } },
      ],
    }),
  Geography: () => <div />,
  Marker: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock('../WallLayers/WallLayers', () => ({
  WallLayers: () => null,
}));

describe('CountryMap', () => {
  it('서울과 광주만 국가 지도에서 노출한다', () => {
    mockState.lastComposableMapProps = null;
    render(<CountryMap onRegionClick={vi.fn()} />);

    expect(screen.getByTestId('country-region-11')).toBeInTheDocument();
    expect(screen.getAllByTestId('country-region-29')).toHaveLength(1);
    expect(screen.getByText('서울')).toBeInTheDocument();
    expect(screen.getByText('광주')).toBeInTheDocument();
    expect(screen.queryByText('세종')).not.toBeInTheDocument();
    expect(screen.queryByText('부산')).not.toBeInTheDocument();
  });

  it('활성 지역이 충분히 크게 보이도록 국가 지도 뷰포트를 집중시킨다', () => {
    mockState.lastComposableMapProps = null;
    render(<CountryMap onRegionClick={vi.fn()} />);

    expect(mockState.lastComposableMapProps).toMatchObject({
      projection: 'geoMercator',
      projectionConfig: {
        scale: 26000,
        center: [126.92, 36.36],
      },
    });
  });
});
