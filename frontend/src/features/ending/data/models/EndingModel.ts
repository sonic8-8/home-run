export interface EndingReportResponseModel {
  characterType: 'MALE' | 'FEMALE';
  endingType: string;
  grade: string;
  title: string;
  totalAssets: number | null;
  totalIncome: number | null;
  totalExpense: number | null;
  netProfit: number | null;
  spendingPattern: {
    topCategory: string | null;
    topCategoryRatio: number | null;
  } | null;
  achievements: EndingAchievementModel[];
  newsHistories: EndingNewsHistoryModel[];
  eventHistories: EndingEventHistoryModel[];
  housingHistories: EndingHousingHistoryModel[];
  housingSnapshot: EndingHousingSnapshotModel | null;
}

export interface EndingAchievementModel {
  name: string;
  iconUrl: string | null;
}

export interface EndingNewsHistoryModel {
  turnNumber: number;
  newsId: string;
  headline: string;
  publishedDate: string;
}

export interface EndingEventHistoryModel {
  turnNumber: number;
  gameEventId: number;
  selectedChoiceCode: string | null;
  resultSummary: string | null;
  resolvedAt: string | null;
}

export interface EndingHousingHistoryModel {
  turnNumber: number;
  summary: string;
  beforeState: EndingHousingStateModel | null;
  afterState: EndingHousingStateModel | null;
}

export interface EndingHousingSnapshotModel {
  currentHousingType: string;
  currentPropertyId: number | null;
  targetPropertyId: number | null;
}

export interface EndingHousingStateModel {
  housingType: string;
  propertyId: number | null;
}

export interface EndingLogsResponseModel {
  timeline: EndingTimelinePointModel[];
}

export interface EndingTimelinePointModel {
  turnNumber: number;
  date: string;
  cash: number | null;
  netAssets: number | null;
  totalAssets: number | null;
  stockValue: number | null;
  loanBalance: number | null;
  salary: number | null;
}
