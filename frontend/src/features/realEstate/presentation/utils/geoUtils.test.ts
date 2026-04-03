import { describe, expect, it } from 'vitest';
import { resolveDistrictCode } from './geoUtils';

describe('resolveDistrictCode', () => {
  it('서울 구 이름을 현재 백엔드 코드로 정규화한다', () => {
    expect(resolveDistrictCode('11', '11230', '강남구')).toBe('11680');
    expect(resolveDistrictCode('11', '11110', '종로구')).toBe('11110');
  });

  it('광주 구 이름을 현재 백엔드 코드로 정규화한다', () => {
    expect(resolveDistrictCode('29', '24040', '북구')).toBe('29170');
  });

  it('알 수 없는 매핑은 기존 코드를 유지한다', () => {
    expect(resolveDistrictCode('99', '99999', '알수없음')).toBe('99999');
  });
});
