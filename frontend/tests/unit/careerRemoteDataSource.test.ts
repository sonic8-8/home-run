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

import { CareerRemoteDataSource } from '@features/career/data/datasources/CareerRemoteDataSource';

describe('CareerRemoteDataSource', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
  });

  it('uses the game sessions endpoint to derive the active session', async () => {
    get.mockResolvedValue({ data: { data: { sessions: [] } } });
    const dataSource = new CareerRemoteDataSource();

    await dataSource.getSessions();

    expect(get).toHaveBeenCalledWith('/games/sessions');
  });

  it('uses the salary negotiation endpoint', async () => {
    post.mockResolvedValue({ data: { data: { success: true } } });
    const dataSource = new CareerRemoteDataSource();

    await dataSource.negotiateSalary(19);

    expect(post).toHaveBeenCalledWith('/games/sessions/19/career/negotiate');
  });

  it('uses the job offers endpoint', async () => {
    get.mockResolvedValue({ data: { data: { offers: [] } } });
    const dataSource = new CareerRemoteDataSource();

    await dataSource.getJobOffers(21);

    expect(get).toHaveBeenCalledWith('/games/sessions/21/career/job-offers');
  });

  it('uses the transfer endpoint with the selected offer id', async () => {
    post.mockResolvedValue({ data: { data: { newJobTitle: 'PM' } } });
    const dataSource = new CareerRemoteDataSource();

    await dataSource.transferJob(24, { offerId: 'offer-1' });

    expect(post).toHaveBeenCalledWith('/games/sessions/24/career/transfer', {
      offerId: 'offer-1',
    });
  });
});
