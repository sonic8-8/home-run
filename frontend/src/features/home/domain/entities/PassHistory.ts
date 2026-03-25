export interface PassHistoryItem {
  historyId: number;
  passName: string;
  amount: number;
  savedAt: string;
}

export interface PassHistoryPage {
  content: PassHistoryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
