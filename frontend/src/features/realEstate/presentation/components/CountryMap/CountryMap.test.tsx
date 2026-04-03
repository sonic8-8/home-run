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
  Geography: ({ geography }: { geography: { rsmKey: string } }) => <div data-testid={`geo-${geography.rsmKey}`} />,
  Marker: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock('../WallLayers/WallLayers', () => ({
  WallLayers: () => null,
}));

describe('CountryMap', () => {
  it('대한민국 전체 배경은 유지하고 서울과 광주만 선택 가능하게 노출한다', () => {
    mockState.lastComposableMapProps = null;
    render(<CountryMap onRegionClick={vi.fn()} />);

    expect(screen.getAllByTestId('geo-seoul').length).toBeGreaterThan(0);
    expect(screen.getAllByTestId('geo-gwangju').length).toBeGreaterThan(0);
    expect(screen.getByTestId('geo-sejong')).toBeInTheDocument();
    expect(screen.getByTestId('geo-busan')).toBeInTheDocument();
    expect(screen.getByTestId('country-region-11')).toBeInTheDocument();
    expect(screen.getAllByTestId('country-region-29')).toHaveLength(1);
    expect(screen.getByText('서울')).toBeInTheDocument();
    expect(screen.getByText('광주')).toBeInTheDocument();
    expect(screen.getByText('세종')).toBeInTheDocument();
    expect(screen.getByText('부산')).toBeInTheDocument();
  });

  it('대한민국 전체 지도가 보이도록 국가 지도 기본 뷰포트를 유지한다', () => {
    mockState.lastComposableMapProps = null;
    render(<CountryMap onRegionClick={vi.fn()} />);

    expect(mockState.lastComposableMapProps).toMatchObject({
      projection: 'geoMercator',
      projectionConfig: {
        scale: 10000,
        center: [127.5, 35.8],
      },
    });
  });
});
