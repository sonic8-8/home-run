import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({
  get: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    get,
  },
}));

import { CardRemoteDataSource } from '@features/card/data/datasources/CardRemoteDataSource';

describe('CardRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
  });

  it('uses the card list endpoint', async () => {
    get.mockResolvedValue({ data: { data: { cards: [] } } });
    const dataSource = new CardRemoteDataSource();

    await dataSource.getCards();

    expect(get).toHaveBeenCalledWith('/cards');
  });

  it('uses the card recommendations endpoint', async () => {
    get.mockResolvedValue({ data: { data: { recommendations: [] } } });
    const dataSource = new CardRemoteDataSource();

    await dataSource.getRecommendations();

    expect(get).toHaveBeenCalledWith('/cards/recommendations');
  });
});
