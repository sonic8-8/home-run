export type SlotStatus =
  | 'EMPTY'
  | 'IN_PROGRESS'
  | 'CLEAR'
  | 'BANKRUPT'
  | 'TIMEOUT'
  | 'FORECLOSURE';

export type JobType =
  | 'LARGE_BIZ'
  | 'MID_BIZ'
  | 'SMALL_BIZ'
  | 'STARTUP'
  | 'FREELANCER';

export interface GameSlot {
  readonly slotNumber: 1 | 2 | 3;
  readonly sessionId: number | null;
  readonly status: SlotStatus;
  readonly characterName?: string;
  readonly jobType?: JobType;
  readonly totalAssets?: number;
  readonly createdAt?: string;
  readonly currentTurn?: number;
}

export interface GameSessionDetail {
  readonly sessionId: number;
  readonly status: Exclude<SlotStatus, 'EMPTY'>;
}
