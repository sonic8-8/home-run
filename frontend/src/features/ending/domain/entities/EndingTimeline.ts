export interface EndingTimelinePoint {
  readonly turnNumber: number;
  readonly date: Date;
  readonly cash: number | null;
  readonly netAssets: number | null;
  readonly totalAssets: number | null;
  readonly stockValue: number | null;
  readonly loanBalance: number | null;
  readonly salary: number | null;
}
