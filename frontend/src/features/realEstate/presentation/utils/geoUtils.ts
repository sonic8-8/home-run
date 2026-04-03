/* eslint-disable @typescript-eslint/no-explicit-any */

import {
  NAME_TO_REGION,
  CODE_TO_REGION,
  DISTRICT_CODE_BY_REGION,
  LABEL_OFFSETS,
  GU_LABEL_OFFSETS,
  PROVINCE_MAP,
} from '../constants/regions';

export function matchRegion(geo: any): string | null {
  const properties = geo.properties || {};

  for (const val of Object.values(properties)) {
    if (typeof val === 'string') {
      const trimmed = val.trim();
      if (NAME_TO_REGION[trimmed]) return NAME_TO_REGION[trimmed];
    }
  }

  const codeRaw = properties.code ?? properties.CODE;
  if (codeRaw != null) {
    const codeStr = String(codeRaw).trim();
    if (codeStr.length >= 2) {
      const prefix = codeStr.slice(0, 2);
      if (CODE_TO_REGION[prefix]) return CODE_TO_REGION[prefix];
    }
  }

  if (geo.id != null) {
    const idStr = String(geo.id).trim();
    if (idStr.length >= 2 && /^\d+$/.test(idStr)) {
      const prefix = idStr.slice(0, 2);
      if (CODE_TO_REGION[prefix]) return CODE_TO_REGION[prefix];
    }
  }

  return null;
}

export function matchByCode(geo: any, codePrefix: string): boolean {
  const properties = geo.properties || {};

  const codeRaw = properties.code ?? properties.CODE;
  if (codeRaw != null) {
    const str = String(codeRaw).trim();
    if (str.startsWith(codePrefix)) return true;
  }

  if (geo.id != null) {
    const idStr = String(geo.id).trim();
    if (/^\d+$/.test(idStr) && idStr.startsWith(codePrefix)) return true;
  }

  const extraKeys = ['adm_cd', 'ADM_CD', 'CTPRVN_CD', 'SIG_CD', 'EMD_CD', 'ADM_SECT_C', 'TOT_REG_CD'];
  for (const key of extraKeys) {
    const val = properties[key];
    if (val != null) {
      const str = String(val).trim();
      if (/^\d+$/.test(str) && str.startsWith(codePrefix)) return true;
    }
  }

  return false;
}

export function getName(properties: Record<string, any>): string {
  return (
    properties.name ||
    properties.NAME ||
    properties.name_eng ||
    properties.NAME_ENG ||
    properties.CTP_KOR_NM ||
    properties.SIG_KOR_NM ||
    properties.EMD_KOR_NM ||
    properties.adm_nm ||
    ''
  );
}

export function getCode(properties: Record<string, any>): string {
  const raw = properties.code ?? properties.CODE ?? '';
  const str = String(raw).trim();
  if (/^\d{2,10}$/.test(str)) return str;

  const keys = ['adm_cd', 'ADM_CD', 'CTPRVN_CD', 'SIG_CD', 'EMD_CD', 'ADM_SECT_C'];
  for (const key of keys) {
    const val = properties[key];
    if (val != null) {
      const s = String(val).trim();
      if (/^\d{2,10}$/.test(s)) return s;
    }
  }
  return '';
}

export function getShortName(name: string): string {
  return PROVINCE_MAP[name] || name;
}

export function computeCentroid(geo: any, fallback?: [number, number]): [number, number] {
  const coords = geo.geometry?.coordinates;
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
    if (count > 0) return [sumLng / count, sumLat / count];
  }
  return fallback || [0, 0];
}

export function getLabelOffset(shortName: string): [number, number] {
  return LABEL_OFFSETS[shortName] || [0, 0];
}

export function getGuLabelOffset(name: string): [number, number] {
  return GU_LABEL_OFFSETS[name] || [0, 0];
}

export function resolveDistrictCode(
  regionCode: string,
  fallbackCode: string,
  districtName: string,
): string {
  return DISTRICT_CODE_BY_REGION[regionCode]?.[districtName] ?? fallbackCode;
}
