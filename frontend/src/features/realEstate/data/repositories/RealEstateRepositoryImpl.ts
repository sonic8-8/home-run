import { inject, injectable } from 'tsyringe';
import { ResponseMappingError, ValidationError } from '@core/error/AppError';
import type {
  IRealEstateRepository,
  PropertyListQuery,
} from '../../domain/repositories/IRealEstateRepository';
import type { PropertyListRequestModel } from '../models/PropertyModel';
import type { Property, PropertySummary, HousingType } from '../../domain/entities/Property';
import type { RegistryDocument, ContractResponse, PurchaseResponse, ContractResult } from '../../domain/entities/PropertyDocument';
import { RealEstateRemoteDataSource } from '../datasources/RealEstateRemoteDataSource';

function toHousingType(value: string): HousingType {
  if (
    value === 'NONE' ||
    value === 'STUDIO' ||
    value === 'VILLA' ||
    value === 'JEONSE_APT' ||
    value === 'OWNED_APT'
  ) {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 주거 형태입니다: ${value}`);
}

function toRegistryVerdict(value: string): '위험' | '정상' {
  if (value === '위험' || value === '정상') {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 등기부 판정입니다: ${value}`);
}

function toContractResult(value: string): ContractResult {
  if (value === 'SAFE' || value === 'TRAPPED' || value === 'PARTIAL') {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 계약 결과입니다: ${value}`);
}

function toPropertyListRequest(query: PropertyListQuery): PropertyListRequestModel {
  if (query.sessionId !== undefined) {
    return {
      sessionId: query.sessionId,
      bounds: query.bounds,
    };
  }

  if (query.regionCode !== undefined && query.districtCode !== undefined) {
    return {
      regionCode: query.regionCode,
      districtCode: query.districtCode,
    };
  }

  throw new ValidationError('매물 조회 조건이 부족합니다.');
}

@injectable()
export class RealEstateRepositoryImpl implements IRealEstateRepository {
  private readonly dataSource: RealEstateRemoteDataSource;
  constructor(
    @inject(RealEstateRemoteDataSource)
    dataSource: RealEstateRemoteDataSource,
  ) { this.dataSource = dataSource; }

  async getProperties(query: PropertyListQuery): Promise<PropertySummary[]> {
    const response = await this.dataSource.getProperties(
      toPropertyListRequest(query),
    );
    return response.properties.map((m) => ({
      propertyId: String(m.propertyId),
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
      housingType: toHousingType(m.housingType),
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
      verdict: toRegistryVerdict(s.verdict),
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
      checklistItems: m.checklistItems.map((item) => ({
        trapId: item.trapId,
        label: item.label,
      })),
      solution: {
        verdict: toRegistryVerdict(m.solution.verdict),
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
      contractResult: toContractResult(m.contractResult),
      message: m.message,
    };
  }
}
