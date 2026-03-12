export interface PassSubscription {
  subscriptionId: number;
  passId: number;
  name: string;
  amountPerSave: number;
  totalSaved: number;
  weeklyHistory: boolean[];
}
