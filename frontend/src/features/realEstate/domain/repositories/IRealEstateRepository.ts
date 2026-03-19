import type { Property, PropertySummary, PropertyDocument, ContractResponse, PurchaseResponse } from '../entities/Property';
import type {} from '../entities/PropertyDocument';

export interface IRealEstateRepository {
  getProperties(sessionId: number, bounds: string): Promise<PropertySummary[]>;
  getPropertyDetail(sessionId: number, propertyId: string): Promise<Property>;
  purchaseProperty(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponse>;
  getDocuments(sessionId: number, propertyId: string): Promise<PropertyDocument[]>;
  contract(sessionId: number, propertyId: string, checkedTraps: string[]): Promise<ContractResponse>;
}
