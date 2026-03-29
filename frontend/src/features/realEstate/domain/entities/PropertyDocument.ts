export interface RegistryRow {
  readonly rankNo: string;
  readonly purpose: string;
  readonly receipt: string;
  readonly reason: string;
  readonly details: string;
}

export interface ContractChecklistItem {
  readonly trapId: string;
  readonly label: string;
}

export interface SectionSolution {
  readonly verdict: '위험' | '정상';
  readonly issueSummary: string;
  readonly keyPoints: readonly string[];
  readonly feedbackCorrect: string;
  readonly feedbackWrong: string;
}

export interface RegistryDocument {
  readonly propertyId: number;
  readonly propertyName: string;
  readonly address: string;
  readonly salePrice: number;
  readonly documentType: string;
  readonly gapguRows: readonly RegistryRow[];
  readonly eulguRows: readonly RegistryRow[];
  readonly checklistItems: readonly ContractChecklistItem[];
  readonly solution: {
    readonly verdict: '위험' | '정상';
    readonly gapgu: SectionSolution;
    readonly eulgu: SectionSolution;
  };
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
