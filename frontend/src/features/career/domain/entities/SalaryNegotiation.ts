export interface SalaryNegotiationResult {
  readonly success: boolean;
  readonly previousSalary: number;
  readonly newSalary: number;
  readonly raiseRate: number;
  readonly lastNegotiatedTurn: number;
  readonly message: string;
}
