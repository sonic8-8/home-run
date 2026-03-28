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
});
