export interface GameSlotModel {
  sessionId: number | null;
  slotNumber: number;
  characterName?: string;
  characterType?: string;
  jobType?: string;
  currentTurn?: number;
  totalAssets?: number;
  createdAt?: string;
  status: 'IN_PROGRESS' | 'EMPTY';
}

export interface GameSessionsResponseModel {
  sessions: GameSlotModel[];
}
