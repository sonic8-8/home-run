export interface SpendingCategoryModel {
  category: string;
  categoryName: string;
  amount: number;
  ratio: number;
}

export interface SpendingResponseModel {
  month: string;
  totalExpense: number;
  categories: SpendingCategoryModel[];
}
