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
  depositItems: { name: string; amount: number }[];
  loanItems: { name: string; amount: number }[];
  otherIncomeItems: { name: string; amount: number }[];
  cardSpendItems: { category: string; amount: number }[];
  paymentTypes: string[];
}

export interface AssetLinkResponseModel {
  isAssetLinked: boolean;
  mainAccountCreated: boolean;
  seedmoneyAccountCreated: boolean;
  summaryInitialized: boolean;
}
