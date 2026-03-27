export interface PassProductModel {
  passId: number;
  name: string;
  amountPerSave: number;
  description: string;
}

export interface PassProductsResponseModel {
  products: PassProductModel[];
}

export interface PassSubscriptionModel {
  subscriptionId: number;
  passId: number;
  name: string;
  amountPerSave: number;
  totalSaved: number;
  weeklyHistory: boolean[];
}

export interface PassSubscriptionsResponseModel {
  subscriptions: PassSubscriptionModel[];
}

export interface PassSubscribeRequestModel {
  passId: number;
}

export interface PassSubscribeResponseModel {
  subscriptionId: number;
  passId: number;
  name: string;
  subscribedAt: string;
}

export interface PassSaveRequestModel {
  subscriptionId: number;
}

export interface PassSaveResponseModel {
  savedAmount: number;
  totalSaved: number;
  subscriptionTotalSaved: number;
  overallTotalSaved: number;
  remainingBalance: number;
}

export interface PassWidgetModel {
  todaySaved: number;
  weeklySaved: number;
  weeklyGoal: number;
  progressRate: number;
  remaining: number;
}

export interface PassHistoryItemModel {
  historyId: number;
  passName: string;
  amount: number;
  savedAt: string;
}

export interface PassHistoryPageModel {
  content: PassHistoryItemModel[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
