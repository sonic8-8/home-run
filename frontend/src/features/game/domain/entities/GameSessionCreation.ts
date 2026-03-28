import type { SlotStatus } from './GameSlot';

export type GameSessionDataSourceType = 'MY_DATA' | 'PROFILE';

export interface GameSessionCreation {
  readonly sessionId: number;
  readonly slotNumber: 1 | 2 | 3;
  readonly status: Exclude<SlotStatus, 'EMPTY'>;
  readonly currentTurn: number;
  readonly dataSourceType: GameSessionDataSourceType;
}
