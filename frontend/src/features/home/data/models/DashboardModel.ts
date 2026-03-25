export interface DashboardResponseModel {
  totalAssets: number;
  monthlyIncome: number;
  monthlyExpense: number;
  incomeChangeFromLastMonth: number | null;
  expenseChangeFromLastMonth: number | null;
  nextPaydayDays: number | null;
  mainAccountBalance: number;
  seedmoneyBalance: number;
}
