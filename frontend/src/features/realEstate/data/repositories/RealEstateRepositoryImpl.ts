import type { IRealEstateRepository } from '../../domain/repositories/IRealEstateRepository';
import type { Property, PropertySummary, HousingType } from '../../domain/entities/Property';
import type { RegistryDocument, ContractResponse, PurchaseResponse, ContractResult } from '../../domain/entities/PropertyDocument';
import { RealEstateRemoteDataSource } from '../datasources/RealEstateRemoteDataSource';

export class RealEstateRepositoryImpl implements IRealEstateRepository {
  private readonly dataSource: RealEstateRemoteDataSource;
  constructor(dataSource: RealEstateRemoteDataSource) { this.dataSource = dataSource; }

  async getProperties(sessionId: number, bounds?: string): Promise<PropertySummary[]> {
    const response = await this.dataSource.getProperties(sessionId, bounds);
    return response.properties.map((m) => ({
      propertyId: m.propertyId,
      name: m.name,
      recentPrice: m.recentPrice,
      latitude: m.latitude,
      longitude: m.longitude,
    }));
  }

  async getPropertyDetail(sessionId: number, propertyId: string): Promise<Property> {
    const m = await this.dataSource.getPropertyDetail(sessionId, propertyId);
    return {
      propertyId: m.propertyId,
      name: m.name,
      recentPrice: m.recentPrice,
      address: m.address,
      latitude: m.latitude,
      longitude: m.longitude,
      housingType: m.housingType as HousingType,
      deposit: m.deposit,
      maintenanceFee: m.maintenanceFee,
      specs: { area: m.specs.area, floor: m.specs.floor, direction: m.specs.direction },
    };
  }

  async purchaseProperty(sessionId: number, propertyId: string, loanId: string): Promise<PurchaseResponse> {
    const m = await this.dataSource.purchaseProperty(sessionId, propertyId, loanId);
    return {
      contractId: m.contractId,
      propertyId: m.propertyId,
      propertyName: m.propertyName,
      purchasePrice: m.purchasePrice,
      loanAmount: m.loanAmount,
      selfFunded: m.selfFunded,
      remainingCash: m.remainingCash,
      housingType: m.housingType,
    };
  }

  async getDocuments(sessionId: number, propertyId: string): Promise<RegistryDocument> {
    const m = await this.dataSource.getDocuments(sessionId, propertyId);
    const mapSection = (s: typeof m.solution.gapgu) => ({
      verdict: s.verdict as '위험' | '정상',
      issueSummary: s.issueSummary,
      keyPoints: s.keyPoints,
      feedbackCorrect: s.feedbackCorrect,
      feedbackWrong: s.feedbackWrong,
    });
    return {
      propertyId: m.propertyId,
      propertyName: m.propertyName,
      address: m.address,
      salePrice: m.salePrice,
      documentType: m.documentType,
      gapguRows: m.gapguRows.map((r) => ({ rankNo: r.rankNo, purpose: r.purpose, receipt: r.receipt, reason: r.reason, details: r.details })),
      eulguRows: m.eulguRows.map((r) => ({ rankNo: r.rankNo, purpose: r.purpose, receipt: r.receipt, reason: r.reason, details: r.details })),
      solution: {
        verdict: m.solution.verdict as '위험' | '정상',
        gapgu: mapSection(m.solution.gapgu),
        eulgu: mapSection(m.solution.eulgu),
      },
    };
  }

  async contract(sessionId: number, propertyId: string, checkedTraps: string[]): Promise<ContractResponse> {
    const m = await this.dataSource.contract(sessionId, propertyId, { checkedTraps });
    return {
      success: m.success,
      trapsDetected: m.trapsDetected,
      trapsCorrectlyIdentified: m.trapsCorrectlyIdentified,
      contractResult: m.contractResult as ContractResult,
      message: m.message,
    };
  }
}
