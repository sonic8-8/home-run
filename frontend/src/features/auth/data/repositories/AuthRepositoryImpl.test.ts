import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthRemoteDataSource } from '../datasources/AuthRemoteDataSource';
import { AuthRepositoryImpl } from './AuthRepositoryImpl';

describe('AuthRepositoryImpl', () => {
  let repository: AuthRepositoryImpl;
  let dataSource: AuthRemoteDataSource;

  beforeEach(() => {
    dataSource = {
      login: vi.fn(),
      signUp: vi.fn<(_: {
        name: string;
        email: string;
        password: string;
        passwordConfirm: string;
        termsAgreed: boolean;
      }) => Promise<void>>(),
    } as unknown as AuthRemoteDataSource;
    repository = new AuthRepositoryImpl(dataSource);
  });

  it('passes the backend-required sign-up fields through unchanged', async () => {
    vi.mocked(dataSource.signUp).mockResolvedValue(undefined);

    await repository.signUp({
      name: '테스터',
      email: 'tester@example.com',
      password: 'Password123!',
      passwordConfirm: 'Password123!',
      termsAgreed: true,
    });

    expect(dataSource.signUp).toHaveBeenCalledWith({
      name: '테스터',
      email: 'tester@example.com',
      password: 'Password123!',
      passwordConfirm: 'Password123!',
      termsAgreed: true,
    });
  });
});
