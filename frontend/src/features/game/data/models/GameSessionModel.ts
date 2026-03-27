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

export interface CreateGameSessionRequestModel {
  slotNumber: number;
  characterType: string;
  characterName: string;
  jobType: string;
  regionCode: string;
  districtCode: string;
  targetPropertyId: number;
  useMyData: boolean;
}

export interface CreateGameSessionResponseModel {
  sessionId: number;
  slotNumber: number;
  sessionStatus:
    | 'IN_PROGRESS'
    | 'CLEAR'
    | 'BANKRUPT'
    | 'TIMEOUT'
    | 'FORECLOSURE';
  currentTurn: number;
  dataSourceType: 'MY_DATA' | 'PROFILE' | 'MANUAL';
}
