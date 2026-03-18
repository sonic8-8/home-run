export interface RealEstate {
  propertyName: string;
  housingType: string;
  currentValue: number;
}

export interface Loan {
  principal: number;
  monthlyInterest: number;
}

export interface StockHolding {
  stockCode: string;
  stockName: string;
  quantity: number;
  currentValue: number;
}

export interface Stock {
  totalValue: number;
  holdings: StockHolding[];
}

export interface Career {
  characterName: string;
  jobType: string;
  jobTitle: string;
  annualSalary: number;
}

export interface SideJob {
  sideJobId: number;
  name: string;
  cashEffect: number;
  healthEffect: number;
}

export interface GameAssets {
  cash: number;
  loan: Loan | null;
  realEstate: RealEstate | null;
  stock: Stock | null;
  career: Career;
  sideJobs: SideJob[];
}
