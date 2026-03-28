import { beforeEach, describe, expect, it, vi } from 'vitest';

const { post } = vi.hoisted(() => ({
  post: vi.fn(),
}));

vi.mock('@core/network/apiClient', () => ({
  apiClient: {
    post,
  },
}));

import { AuthRemoteDataSource } from './AuthRemoteDataSource';

describe('AuthRemoteDataSource', () => {
  beforeEach(() => {
    post.mockReset();
  });

  it('clears Authorization header on login requests', async () => {
    post.mockResolvedValue({ data: { data: { accessToken: 'token' } } });
    const dataSource = new AuthRemoteDataSource();

    await dataSource.login({
      email: 'tester@example.com',
      password: 'Password123!',
    });

    expect(post).toHaveBeenCalledWith(
      '/auth/login',
      {
        email: 'tester@example.com',
        password: 'Password123!',
      },
      {
        headers: { Authorization: undefined },
      },
    );
  });

  it('clears Authorization header on sign-up requests', async () => {
    post.mockResolvedValue({ data: { data: null } });
    const dataSource = new AuthRemoteDataSource();

    await dataSource.signUp({
      email: 'tester@example.com',
      password: 'Password123!',
      passwordConfirm: 'Password123!',
      name: '테스터',
      termsAgreed: true,
    });

    expect(post).toHaveBeenCalledWith(
      '/auth/signup',
      {
        email: 'tester@example.com',
        password: 'Password123!',
        passwordConfirm: 'Password123!',
        name: '테스터',
        termsAgreed: true,
      },
      {
        headers: { Authorization: undefined },
      },
    );
  });
});
