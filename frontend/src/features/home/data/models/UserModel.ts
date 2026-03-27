export interface UserMeResponseModel {
  userId: number;
  email: string;
  name: string;
  isAssetLinked: boolean;
  totalAssetAmount: number | null;
  netAssetAmount: number | null;
}

export interface AssetLinkRequestModel {
  mainAccountBalanceAmount: number;
  salaryDayOfMonth: number;
  monthlySalaryAmount: number;
  monthlyFixedExpenseAmount: number;
  jobType: string;
  depositItems: { balance: number }[];
  loanItems: { balance: number }[];
  otherIncomeItems: { amount: number }[];
  cardSpendItems: { category: string; monthlyAmount: number }[];
}

export interface AssetLinkResponseModel {
  isAssetLinked: boolean;
  mainAccountCreated: boolean;
  seedmoneyAccountCreated: boolean;
  summaryInitialized: boolean;
}
