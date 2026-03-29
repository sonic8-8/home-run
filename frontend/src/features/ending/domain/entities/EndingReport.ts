export type EndingType =
  | 'IN_PROGRESS'
  | 'CLEAR'
  | 'BANKRUPT'
  | 'TIMEOUT'
  | 'FORECLOSURE';

export type CharacterType = 'MALE' | 'FEMALE';

export type HousingType =
  | 'NONE'
  | 'STUDIO'
  | 'VILLA'
  | 'JEONSE_APT'
  | 'OWNED_APT';

export interface EndingAchievement {
  readonly name: string;
  readonly iconUrl: string | null;
}

export interface EndingNewsHistory {
  readonly turnNumber: number;
  readonly newsId: string;
  readonly headline: string;
  readonly publishedDate: Date;
}

export interface EndingEventHistory {
  readonly turnNumber: number;
  readonly gameEventId: number;
  readonly selectedChoiceCode: string | null;
  readonly resultSummary: string | null;
  readonly resolvedAt: Date | null;
}

export interface EndingHousingState {
  readonly housingType: HousingType;
  readonly propertyId: number | null;
}

export interface EndingHousingHistory {
  readonly turnNumber: number;
  readonly summary: string;
  readonly beforeState: EndingHousingState | null;
  readonly afterState: EndingHousingState | null;
}

export interface EndingHousingSnapshot {
  readonly currentHousingType: HousingType;
  readonly currentPropertyId: number | null;
  readonly targetPropertyId: number | null;
}

export interface EndingSpendingPattern {
  readonly topCategory: string | null;
  readonly topCategoryRatio: number | null;
}

export interface EndingReport {
  readonly characterType: CharacterType;
  readonly endingType: EndingType;
  readonly grade: string;
  readonly title: string;
  readonly totalAssets: number | null;
  readonly totalIncome: number | null;
  readonly totalExpense: number | null;
  readonly netProfit: number | null;
  readonly spendingPattern: EndingSpendingPattern | null;
  readonly achievements: readonly EndingAchievement[];
  readonly newsHistories: readonly EndingNewsHistory[];
  readonly eventHistories: readonly EndingEventHistory[];
  readonly housingHistories: readonly EndingHousingHistory[];
  readonly housingSnapshot: EndingHousingSnapshot | null;
}
