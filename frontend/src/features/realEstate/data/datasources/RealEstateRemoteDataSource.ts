import { injectable } from 'tsyringe';
import type {
  PropertiesResponseModel,
  PropertyModel,
  PurchaseResponseModel,
  DocumentsResponseModel,
  ContractRequestModel,
  ContractResponseModel,
} from '../models/PropertyModel';

@injectable()
export class RealEstateRemoteDataSource {
  async getProperties(_sessionId: number, _bounds: string): Promise<PropertiesResponseModel> {
    // TODO: return apiClient.get(`/games/sessions/${_sessionId}/real-estate/properties?bounds=${_bounds}`)
    // Mock: 서울 강남구 2개 매물 (서울 드릴다운 시 강남구 선택 → 네이버 지도에서 확인 가능)
    return {
      properties: [
        { propertyId: 'PROP-GN-001', name: '래미안 강남포레스트', recentPrice: 1_350_000_000, latitude: 37.5094, longitude: 127.0571 },
        { propertyId: 'PROP-GN-002', name: '강남 힐스테이트 에코', recentPrice: 720_000_000, latitude: 37.5200, longitude: 127.0290 },
      ],
    };
  }

  async getPropertyDetail(_sessionId: number, propertyId: string): Promise<PropertyModel> {
    // TODO: return apiClient.get(`/games/sessions/${_sessionId}/real-estate/properties/${propertyId}`)
    const mock: Record<string, PropertyModel> = {
      'PROP-GN-001': {
        propertyId: 'PROP-GN-001',
        name: '래미안 강남포레스트',
        recentPrice: 1_350_000_000,
        address: '서울특별시 강남구 개포동 1300',
        latitude: 37.5094,
        longitude: 127.0571,
        housingType: 'OWNED_APT',
        deposit: 0,
        maintenanceFee: 380_000,
        specs: { area: 84.9, floor: '12/28', direction: '남향' },
      },
      'PROP-GN-002': {
        propertyId: 'PROP-GN-002',
        name: '강남 힐스테이트 에코',
        recentPrice: 720_000_000,
        address: '서울특별시 강남구 역삼동 815-1',
        latitude: 37.5200,
        longitude: 127.0290,
        housingType: 'JEONSE_APT',
        deposit: 500_000_000,
        maintenanceFee: 220_000,
        specs: { area: 59.8, floor: '7/20', direction: '동향' },
      },
    };
    return mock[propertyId] ?? mock['PROP-GN-001'];
  }

  async purchaseProperty(_sessionId: number, propertyId: string, _loanId: string): Promise<PurchaseResponseModel> {
    // TODO: return apiClient.post(`/games/sessions/${_sessionId}/real-estate/properties/${propertyId}/purchase`, { loanId: _loanId })
    return {
      contractId: 'CONTRACT-001',
      propertyId,
      propertyName: '래미안 강남포레스트',
      purchasePrice: 1_350_000_000,
      loanAmount: 945_000_000,
      selfFunded: 405_000_000,
      remainingCash: 1_000_000,
      housingType: 'OWNED_APT',
    };
  }

  async getDocuments(_sessionId: number, _propertyId: string): Promise<DocumentsResponseModel> {
    // TODO: return apiClient.get(`/games/sessions/${_sessionId}/real-estate/properties/${_propertyId}/documents`)
    return {
      documents: [
        {
          documentId: 1,
          type: '등기사항전부증명서',
          imageUrl: '/images/docs/registry.png',
          checklist: [
            { trapId: 'TRAP-01', label: '근저당 설정 확인', isTrapped: true },
            { trapId: 'TRAP-02', label: '소유자 일치 확인', isTrapped: false },
          ],
        },
      ],
    };
  }

  async contract(_sessionId: number, _propertyId: string, _body: ContractRequestModel): Promise<ContractResponseModel> {
    // TODO: return apiClient.post(`/games/sessions/${_sessionId}/real-estate/properties/${_propertyId}/contract`, _body)
    return {
      success: true,
      trapsDetected: 1,
      trapsCorrectlyIdentified: 1,
      contractResult: 'SAFE',
      message: '계약이 성공적으로 완료되었습니다.',
    };
  }
}
