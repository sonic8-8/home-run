export interface CareerSessionSlotModel {
  sessionId: number | null;
  slotNumber: number;
  characterName?: string;
  jobType?: string;
  currentTurn?: number;
  status:
    | 'EMPTY'
    | 'IN_PROGRESS'
    | 'CLEAR'
    | 'BANKRUPT'
    | 'TIMEOUT'
    | 'FORECLOSURE';
}

export interface CareerSessionsResponseModel {
  sessions: CareerSessionSlotModel[];
}

export interface SalaryNegotiationResponseModel {
  success: boolean;
  previousSalary: number;
  newSalary: number;
  raiseRate: number;
  lastNegotiatedTurn: number;
  message: string;
}

export interface JobOfferModel {
  offerId: string;
  jobType: string;
  companyName: string;
  currentSalary: number;
  offeredSalary: number;
  probationTurns: number | null;
}

export interface JobOfferListResponseModel {
  offers: JobOfferModel[];
  offerChanceBonusRate: number;
  meetFriendBonusApplied: boolean;
}

export interface JobTransferRequestModel {
  offerId: string;
}

export interface JobTransferResponseModel {
  previousJobType: string;
  newJobType: string;
  newJobTitle: string;
  newSalary: number;
  probationEndTurn: number | null;
  tenureReset: boolean;
  message: string;
}
