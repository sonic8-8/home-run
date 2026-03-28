import type { EconomicCycleType } from '@features/game/domain/entities/GameTurn';

const ECONOMIC_CYCLE_LABELS: Record<EconomicCycleType, string> = {
  BOOM_TO_BOOM: '호황 지속',
  BOOM_TO_CRISIS: '호황 → 위기',
  BOOM_TO_RECOVERY: '호황 → 회복',
  CRISIS_TO_CRISIS: '위기 지속',
  CRISIS_TO_RECOVERY: '위기 → 회복',
  CRISIS_TO_BOOM: '위기 → 호황',
  RECOVERY_TO_BOOM: '회복 → 호황',
  RECOVERY_TO_RECOVERY: '회복 지속',
  RECOVERY_TO_CRISIS: '회복 → 위기',
  EXPANSION: '경기 확장',
  CONTRACTION: '경기 수축',
  RECOVERY: '경기 회복',
  PEAK: '경기 정점',
  TROUGH: '경기 저점',
};

const NEGATIVE_ECONOMIC_CYCLE_TYPES = new Set<EconomicCycleType>([
  'BOOM_TO_CRISIS',
  'CRISIS_TO_CRISIS',
  'RECOVERY_TO_CRISIS',
  'CONTRACTION',
  'TROUGH',
]);

export function getEconomicCycleLabel(type: EconomicCycleType): string {
  return ECONOMIC_CYCLE_LABELS[type];
}

export function isNegativeEconomicCycle(type: EconomicCycleType): boolean {
  return NEGATIVE_ECONOMIC_CYCLE_TYPES.has(type);
}
