import { describe, expect, it, vi } from 'vitest';
import type { RealEstateRemoteDataSource } from '../datasources/RealEstateRemoteDataSource';
import { RealEstateRepositoryImpl } from './RealEstateRepositoryImpl';

describe('RealEstateRepositoryImpl', () => {
  it('throws when property query information is missing', async () => {
    const dataSource = {
      getProperties: vi.fn(),
    } as Pick<RealEstateRemoteDataSource, 'getProperties'> as RealEstateRemoteDataSource;
    const repository = new RealEstateRepositoryImpl(dataSource);

    await expect(
      repository.getProperties({}),
    ).rejects.toThrow('매물 조회 조건이 부족합니다.');
    expect(dataSource.getProperties).not.toHaveBeenCalled();
  });

  it('maps checklist items from registry documents', async () => {
    const dataSource = {
      getDocuments: vi.fn().mockResolvedValue({
        propertyId: 7,
        propertyName: '서초아트자이',
        address: '서울특별시 서초구 반포대로 58',
        latitude: 37.485551,
        longitude: 127.0115,
        salePrice: 1_300_000_000,
        documentType: '등기사항전부증명서',
        gapguRows: [],
        eulguRows: [],
        checklistItems: [
          { trapId: 'TRAP-HN-001', label: '소유권 변동 이력 확인' },
          { trapId: 'CHECK-HN-001', label: '가등기 말소 여부 확인' },
        ],
        solution: {
          verdict: '위험',
          gapgu: {
            verdict: '위험',
            issueSummary: '갑구 해설',
            keyPoints: ['갑구 포인트'],
            feedbackCorrect: '갑구 정답 해설',
            feedbackWrong: '갑구 오답 해설',
          },
          eulgu: {
            verdict: '정상',
            issueSummary: '을구 해설',
            keyPoints: ['을구 포인트'],
            feedbackCorrect: '을구 정답 해설',
            feedbackWrong: '을구 오답 해설',
          },
        },
      }),
    } as Pick<RealEstateRemoteDataSource, 'getDocuments'> as RealEstateRemoteDataSource;
    const repository = new RealEstateRepositoryImpl(dataSource);

    await expect(
      repository.getDocuments(7, '11'),
    ).resolves.toMatchObject({
      checklistItems: [
        { trapId: 'TRAP-HN-001', label: '소유권 변동 이력 확인' },
        { trapId: 'CHECK-HN-001', label: '가등기 말소 여부 확인' },
      ],
    });
  });

  it('maps contract review response values', async () => {
    const dataSource = {
      contract: vi.fn().mockResolvedValue({
        success: false,
        trapsDetected: 2,
        trapsCorrectlyIdentified: 1,
        contractResult: 'PARTIAL',
        message: '주의가 필요한 항목이 있습니다.',
      }),
    } as Pick<RealEstateRemoteDataSource, 'contract'> as RealEstateRemoteDataSource;
    const repository = new RealEstateRepositoryImpl(dataSource);

    await expect(
      repository.contract(7, '11', ['TRAP-HN-001']),
    ).resolves.toMatchObject({
      success: false,
      trapsDetected: 2,
      trapsCorrectlyIdentified: 1,
      contractResult: 'PARTIAL',
      message: '주의가 필요한 항목이 있습니다.',
    });
  });
});
