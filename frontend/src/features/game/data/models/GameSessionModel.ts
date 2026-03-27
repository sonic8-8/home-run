export interface GameSlotModel {
  sessionId: number | null;
  slotNumber: number;
  characterName?: string;
  characterType?: string;
  jobType?: string;
  currentTurn?: number;
  totalAssets?: number;
  createdAt?: string;
  status:
    | 'EMPTY'
    | 'IN_PROGRESS'
    | 'CLEAR'
    | 'BANKRUPT'
    | 'TIMEOUT'
    | 'FORECLOSURE';
}

export interface GameSessionsResponseModel {
  sessions: GameSlotModel[];
}
