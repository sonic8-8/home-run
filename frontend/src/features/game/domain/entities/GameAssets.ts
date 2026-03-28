import type { JobType } from '@features/game/domain/entities/GameSlot';

export type HousingType =
  | 'NONE'
  | 'STUDIO'
  | 'VILLA'
  | 'JEONSE_APT'
  | 'OWNED_APT';

export interface RealEstate {
  readonly propertyName: string;
  readonly housingType: HousingType;
  readonly currentValue: number;
}

export interface Loan {
  readonly principal: number;
  readonly monthlyInterest: number;
}

export interface StockHolding {
  readonly stockCode: string;
  readonly stockName: string;
  readonly quantity: number;
  readonly currentValue: number;
}

export interface Stock {
  readonly totalValue: number;
  readonly holdings: readonly StockHolding[];
}

export interface Career {
  readonly characterName: string;
  readonly jobType: JobType;
  readonly jobTitle: string;
  readonly annualSalary: number;
}

export interface SideJob {
  readonly sideJobId: number;
  readonly name: string;
  readonly cashEffect: number;
  readonly healthEffect: number;
}

export interface GameAssets {
  readonly cash: number;
  readonly loan: Loan | null;
  readonly realEstate: RealEstate | null;
  readonly stock: Stock | null;
  readonly career: Career;
  readonly sideJobs: readonly SideJob[];
}
