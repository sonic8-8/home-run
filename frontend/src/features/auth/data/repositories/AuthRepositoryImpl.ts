import { inject, injectable } from 'tsyringe';
import type { LoginCredentials } from '@features/auth/domain/entities/LoginCredentials';
import type { LoginSession } from '@features/auth/domain/entities/LoginSession';
import type { SignUpCredentials } from '@features/auth/domain/entities/SignUpCredentials';
import type { IAuthRepository } from '@features/auth/domain/repositories/IAuthRepository';
import { AuthRemoteDataSource } from '../datasources/AuthRemoteDataSource';

@injectable()
export class AuthRepositoryImpl implements IAuthRepository {
  private readonly dataSource: AuthRemoteDataSource;

  constructor(
    @inject(AuthRemoteDataSource)
    dataSource: AuthRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async login(credentials: LoginCredentials): Promise<LoginSession> {
    const model = await this.dataSource.login({
      email: credentials.email,
      password: credentials.password,
    });

    return {
      accessToken: model.accessToken,
      refreshToken: model.refreshToken,
      accessTokenExpiresIn: model.accessTokenExpiresIn,
      name: model.name,
    };
  }

  async signUp(credentials: SignUpCredentials): Promise<void> {
    await this.dataSource.signUp({
      name: credentials.name,
      email: credentials.email,
      password: credentials.password,
    });
  }
}
