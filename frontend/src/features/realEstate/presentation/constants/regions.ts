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
    code: '29',
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
  '29': '광주광역시',
};

export const DISTRICT_CODE_BY_REGION: Record<string, Record<string, string>> = {
  '11': {
    종로구: '11110',
    중구: '11140',
    용산구: '11170',
    성동구: '11200',
    광진구: '11215',
    동대문구: '11230',
    중랑구: '11260',
    성북구: '11290',
    강북구: '11305',
    도봉구: '11320',
    노원구: '11350',
    은평구: '11380',
    서대문구: '11410',
    마포구: '11440',
    양천구: '11470',
    강서구: '11500',
    구로구: '11530',
    금천구: '11545',
    영등포구: '11560',
    동작구: '11590',
    관악구: '11620',
    서초구: '11650',
    강남구: '11680',
    송파구: '11710',
    강동구: '11740',
  },
  '29': {
    동구: '29110',
    서구: '29140',
    남구: '29155',
    북구: '29170',
    광산구: '29200',
  },
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
