import type { Property, PropertySummary } from '../entities/Property';
import type { RegistryDocument, ContractResponse, PurchaseResponse } from '../entities/PropertyDocument';

export interface PropertyListQuery {
  readonly sessionId?: number;
  readonly regionCode?: string;
  readonly districtCode?: string;
  readonly bounds?: string;
}

export interface IRealEstateRepository {
  getProperties(query: PropertyListQuery): Promise<PropertySummary[]>;
  getPropertyDetail(sessionId: number, propertyId: string): Promise<Property>;
  purchaseProperty(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponse>;
  getDocuments(sessionId: number, propertyId: string): Promise<RegistryDocument>;
  contract(sessionId: number, propertyId: string, checkedTraps: string[]): Promise<ContractResponse>;
}
