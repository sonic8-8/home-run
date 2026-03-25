export interface SpendingCategory {
  category: string;
  categoryName: string;
  amount: number;
  ratio: number;
}

export interface Spending {
  month: string;
  totalExpense: number;
  categories: SpendingCategory[];
}
