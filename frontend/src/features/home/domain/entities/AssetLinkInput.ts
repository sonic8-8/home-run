export type JobType = 'SMALL_BIZ' | 'MID_BIZ' | 'LARGE_BIZ' | 'STARTUP' | 'FREELANCER';
export type CardSpendCategory = 'LIVING' | 'TRANSPORT' | 'TELECOM' | 'FUEL' | 'MART' | 'EDUCATION' | 'OVERSEAS';

export interface NamedAmountItem {
  name: string;
  amount: number;
}

export interface CardSpendItem {
  category: CardSpendCategory;
  amount: number;
}

export interface AssetLinkInput {
  mainAccountBalanceAmount: number;
  salaryDayOfMonth: number;
  monthlySalaryAmount: number;
  monthlyFixedExpenseAmount: number;
  jobType: JobType;
  depositItems: NamedAmountItem[];
  loanItems: NamedAmountItem[];
  otherIncomeItems: NamedAmountItem[];
  cardSpendItems: CardSpendItem[];
  paymentTypes: string[];
}
