/* eslint-disable @typescript-eslint/no-explicit-any */

export interface RegionData {
  color: string;
  sideColor: string;
  code: string;
  center: [number, number];
  scale: number;
  label: string;
}

export const GEO_PROVINCES =
  'https://raw.githubusercontent.com/southkorea/southkorea-maps/master/kostat/2018/json/skorea-provinces-2018-topo-simple.json';
export const GEO_MUNICIPALITIES =
  'https://raw.githubusercontent.com/southkorea/southkorea-maps/master/kostat/2018/json/skorea-municipalities-2018-topo-simple.json';

export const ACTIVE_REGIONS: Record<string, RegionData> = {
  서울특별시: {
    color: '#60a5fa',
    sideColor: '#3b82f6',
    code: '11',
    center: [126.98, 37.56],
    scale: 160000,
    label: '서울',
  },
  광주광역시: {
    color: '#60a5fa',
    sideColor: '#3b82f6',
    code: '24',
    center: [126.855, 35.16],
    scale: 260000,
    label: '광주',
  },
};

export const NAME_TO_REGION: Record<string, string> = {
  서울특별시: '서울특별시',
  서울: '서울특별시',
  Seoul: '서울특별시',
  광주광역시: '광주광역시',
  광주: '광주광역시',
  Gwangju: '광주광역시',
};

export const CODE_TO_REGION: Record<string, string> = {
  '11': '서울특별시',
  '24': '광주광역시',
};

export const LABEL_OFFSETS: Record<string, [number, number]> = {
  경기: [0.15, -0.25],
  인천: [-0.15, 0],
  충남: [-0.1, 0],
};

export const GU_LABEL_OFFSETS: Record<string, [number, number]> = {
  종로구: [0.01, -0.015],
};

export const PROVINCE_MAP: Record<string, string> = {
  서울특별시: '서울',
  부산광역시: '부산',
  대구광역시: '대구',
  인천광역시: '인천',
  광주광역시: '광주',
  대전광역시: '대전',
  울산광역시: '울산',
  세종특별자치시: '세종',
  경기도: '경기',
  강원도: '강원',
  충청북도: '충북',
  충청남도: '충남',
  전라북도: '전북',
  전라남도: '전남',
  경상북도: '경북',
  경상남도: '경남',
  제주특별자치도: '제주',
};

export const LIFT_HEIGHT = 18;
export const WALL_LAYERS = 16;
export const TRANSITION = 'all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)';
export const TRANSITION_FADE = 'all 0.3s ease-out';
