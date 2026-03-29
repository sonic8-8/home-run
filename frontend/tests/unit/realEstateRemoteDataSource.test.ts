import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get, post } = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
    post,
  },
}));

import { RealEstateRemoteDataSource } from '@features/realEstate/data/datasources/RealEstateRemoteDataSource';

describe('RealEstateRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
  });

  it('uses the session property endpoint when sessionId exists', async () => {
    get.mockResolvedValue({ data: { data: { properties: [] } } });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.getProperties({
      sessionId: 7,
      bounds: '126.90,37.40,127.10,37.60',
    });

    expect(get).toHaveBeenCalledWith(
      '/games/sessions/7/real-estate/properties',
      { params: { bounds: '126.90,37.40,127.10,37.60' } },
    );
  });

  it('uses the target property endpoint before a session exists', async () => {
    get.mockResolvedValue({ data: { data: { properties: [] } } });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.getProperties({
      regionCode: '11',
      districtCode: '11680',
    });

    expect(get).toHaveBeenCalledWith(
      '/games/regions/11/districts/11680/properties',
    );
  });

  it('loads registry documents from the session-scoped endpoint', async () => {
    get.mockResolvedValue({ data: { data: { checklistItems: [] } } });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.getDocuments(7, '11');

    expect(get).toHaveBeenCalledWith(
      '/games/sessions/7/real-estate/properties/11/documents',
    );
  });

  it('posts checked traps to the contract review endpoint', async () => {
    post.mockResolvedValue({ data: { data: { success: true } } });
    const dataSource = new RealEstateRemoteDataSource();

    await dataSource.contract(7, '11', {
      checkedTraps: ['TRAP-HN-001'],
    });

    expect(post).toHaveBeenCalledWith(
      '/games/sessions/7/real-estate/properties/11/contract',
      { checkedTraps: ['TRAP-HN-001'] },
    );
  });
});
