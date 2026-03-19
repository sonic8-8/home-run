export interface PropertySummaryModel {
  propertyId: string;
  name: string;
  recentPrice: number;
  latitude: number;
  longitude: number;
}

export interface PropertiesResponseModel {
  properties: PropertySummaryModel[];
}

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

export interface TrapModel {
  trapId: string;
  label: string;
  isTrapped: boolean;
}

export interface DocumentModel {
  documentId: number;
  type: string;
  imageUrl: string;
  checklist: TrapModel[];
}

export interface DocumentsResponseModel {
  documents: DocumentModel[];
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
