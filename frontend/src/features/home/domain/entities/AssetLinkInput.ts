export type JobType = 'SMALL_BIZ' | 'MID_BIZ' | 'LARGE_BIZ' | 'STARTUP' | 'FREELANCER';
export type CardSpendCategory = 'LIVING' | 'TRANSPORT' | 'TELECOM' | 'FUEL' | 'MART' | 'EDUCATION' | 'OVERSEAS';

export interface DepositItem {
  balance: number;
}

export interface LoanItem {
  balance: number;
}

export interface OtherIncomeItem {
  amount: number;
}

export interface CardSpendItem {
  category: CardSpendCategory;
  monthlyAmount: number;
}

export interface AssetLinkInput {
  mainAccountBalanceAmount: number;
  salaryDayOfMonth: number;
  monthlySalaryAmount: number;
  monthlyFixedExpenseAmount: number;
  jobType: JobType;
  depositItems: DepositItem[];
  loanItems: LoanItem[];
  otherIncomeItems: OtherIncomeItem[];
  cardSpendItems: CardSpendItem[];
}
