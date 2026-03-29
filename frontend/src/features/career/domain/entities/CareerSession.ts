export type CareerJobType =
  | 'LARGE_BIZ'
  | 'MID_BIZ'
  | 'SMALL_BIZ'
  | 'STARTUP'
  | 'FREELANCER';

export interface CareerSession {
  readonly sessionId: number;
  readonly slotNumber: number;
  readonly characterName: string | null;
  readonly jobType: CareerJobType | null;
  readonly currentTurn: number | null;
}
