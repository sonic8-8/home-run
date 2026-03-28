export interface PropertySummaryModel {
  propertyId: string | number;
  name: string;
  recentPrice: number;
  latitude: number;
  longitude: number;
}

export interface PropertiesResponseModel {
  properties: PropertySummaryModel[];
}

export type PropertyListRequestModel =
  | {
      sessionId: number;
      bounds?: string;
      regionCode?: never;
      districtCode?: never;
    }
  | {
      regionCode: string;
      districtCode: string;
      sessionId?: never;
      bounds?: never;
    };

export interface PropertySpecsModel {
  area: number;
  floor: string;
  direction: string;
}

export interface PropertyModel {
  propertyId: string;
  name: string;
  recentPrice: number;
  address: string;
  latitude: number;
  longitude: number;
  housingType: string;
  deposit: number;
  maintenanceFee: number;
  specs: PropertySpecsModel;
}

export interface RegistryRowModel {
  rankNo: string;
  purpose: string;
  receipt: string;
  reason: string;
  details: string;
}

export interface SectionSolutionModel {
  verdict: string;
  issueSummary: string;
  keyPoints: string[];
  feedbackCorrect: string;
  feedbackWrong: string;
}

export interface RegistryDocumentResponseModel {
  propertyId: number;
  propertyName: string;
  address: string;
  latitude: number;
  longitude: number;
  salePrice: number;
  documentType: string;
  gapguRows: RegistryRowModel[];
  eulguRows: RegistryRowModel[];
  solution: {
    verdict: string;
    gapgu: SectionSolutionModel;
    eulgu: SectionSolutionModel;
  };
}

export interface PurchaseRequestModel {
  loanId: string;
}

export interface PurchaseResponseModel {
  contractId: string;
  propertyId: string;
  propertyName: string;
  purchasePrice: number;
  loanAmount: number;
  selfFunded: number;
  remainingCash: number;
  housingType: string;
}

export interface ContractRequestModel {
  checkedTraps: string[];
}

export interface ContractResponseModel {
  success: boolean;
  trapsDetected: number;
  trapsCorrectlyIdentified: number;
  contractResult: string;
  message: string;
}
