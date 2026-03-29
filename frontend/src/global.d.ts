interface NaverMapsApi {
  LatLng: new (lat: number, lng: number) => unknown;
  Map: new (element: HTMLElement, options: Record<string, unknown>) => unknown;
  Marker: new (options: Record<string, unknown>) => {
    setMap: (map: unknown) => void;
    setZIndex: (zIndex: number) => void;
    setIcon: (icon: Record<string, unknown>) => void;
  };
  Point: new (x: number, y: number) => unknown;
  Event: {
    addListener: (target: unknown, eventName: string, handler: () => void) => unknown;
    removeListener: (listener: unknown) => void;
  };
  Position: {
    TOP_RIGHT: unknown;
    BOTTOM_LEFT: unknown;
  };
  ZoomControlStyle: {
    SMALL: unknown;
  };
}

declare global {
  interface Window {
    naver?: {
      maps?: NaverMapsApi;
    };
  }
}

export {};
