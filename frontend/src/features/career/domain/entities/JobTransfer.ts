import type { CareerJobType } from './CareerSession';

export interface JobTransferResult {
  readonly previousJobType: CareerJobType;
  readonly newJobType: CareerJobType;
  readonly newJobTitle: string;
  readonly newSalary: number;
  readonly probationEndTurn: number | null;
  readonly tenureReset: boolean;
  readonly message: string;
}
