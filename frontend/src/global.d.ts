type NaverLatLng = object;

type NaverPoint = object;

type NaverEventListener = object;

type NaverMapInstance = object;

interface NaverMapIcon {
  content: string;
  anchor: NaverPoint;
}

interface NaverMapOptions {
  center: NaverLatLng;
  zoom: number;
  zoomControl: boolean;
  zoomControlOptions: {
    position: unknown;
    style: unknown;
  };
  mapTypeControl: boolean;
  scaleControl: boolean;
  logoControl: boolean;
  logoControlOptions: {
    position: unknown;
  };
}

interface NaverMarkerOptions {
  position: NaverLatLng;
  map: NaverMapInstance | null;
  zIndex: number;
  icon: NaverMapIcon;
}

interface NaverMapMarker {
  setMap: (map: NaverMapInstance | null) => void;
  setZIndex: (zIndex: number) => void;
  setIcon: (icon: NaverMapIcon) => void;
}

interface NaverMapsApi {
  LatLng: new (lat: number, lng: number) => NaverLatLng;
  Map: new (element: HTMLElement, options: NaverMapOptions) => NaverMapInstance;
  Marker: new (options: NaverMarkerOptions) => NaverMapMarker;
  Point: new (x: number, y: number) => NaverPoint;
  Event: {
    addListener: (target: object, eventName: string, handler: () => void) => NaverEventListener;
    removeListener: (listener: NaverEventListener) => void;
  };
  Position: {
    TOP_RIGHT: unknown;
    BOTTOM_LEFT: unknown;
  };
  ZoomControlStyle: {
    SMALL: unknown;
  };
}

interface Window {
  naver?: {
    maps?: NaverMapsApi;
  };
}
