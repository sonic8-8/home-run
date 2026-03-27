import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({
  get: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
  },
}));

import { RealEstateRemoteDataSource } from '@features/realEstate/data/datasources/RealEstateRemoteDataSource';

describe('RealEstateRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
  });

  it('uses the session property endpoint when sessionId exists', async () => {
    get.mockResolvedValue({ properties: [] });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.getProperties({
      sessionId: 7,
      bounds: '126.90,37.40,127.10,37.60',
    });

    expect(get).toHaveBeenCalledWith(
      '/api/games/sessions/7/real-estate/properties?bounds=126.90%2C37.40%2C127.10%2C37.60',
    );
  });

  it('uses the target property endpoint before a session exists', async () => {
    get.mockResolvedValue({ properties: [] });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.getProperties({
      regionCode: '11',
      districtCode: '11680',
    });

    expect(get).toHaveBeenCalledWith(
      '/api/games/regions/11/districts/11680/properties',
    );
  });

  it('throws when property query information is missing', async () => {
    const dataSource = new RealEstateRemoteDataSource();

    await expect(dataSource.getProperties({})).rejects.toThrow(
      '매물 조회 조건이 부족합니다.',
    );
    expect(get).not.toHaveBeenCalled();
  });
});
