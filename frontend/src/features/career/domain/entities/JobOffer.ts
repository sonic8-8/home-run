import type { CareerJobType } from './CareerSession';

export interface JobOffer {
  readonly offerId: string;
  readonly jobType: CareerJobType;
  readonly companyName: string;
  readonly currentSalary: number;
  readonly offeredSalary: number;
  readonly probationTurns: number | null;
}

export interface JobOfferList {
  readonly offers: readonly JobOffer[];
  readonly offerChanceBonusRate: number;
  readonly meetFriendBonusApplied: boolean;
}
