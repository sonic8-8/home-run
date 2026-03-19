export interface Trap {
  readonly trapId: string;
  readonly label: string;
  readonly isTrapped: boolean;
}

export interface PropertyDocument {
  readonly documentId: number;
  readonly type: string;
  readonly imageUrl: string;
  readonly checklist: readonly Trap[];
}

export type ContractResult = 'SAFE' | 'TRAPPED' | 'PARTIAL';

export interface ContractResponse {
  readonly success: boolean;
  readonly trapsDetected: number;
  readonly trapsCorrectlyIdentified: number;
  readonly contractResult: ContractResult;
  readonly message: string;
}

export interface PurchaseResponse {
  readonly contractId: string;
  readonly propertyId: string;
  readonly propertyName: string;
  readonly purchasePrice: number;
  readonly loanAmount: number;
  readonly selfFunded: number;
  readonly remainingCash: number;
  readonly housingType: string;
}
