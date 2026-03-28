import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { LoginCredentials } from '../entities/LoginCredentials';
import type { LoginSession } from '../entities/LoginSession';
import type { IAuthRepository } from '../repositories/IAuthRepository';

@injectable()
export class LoginUseCase {
  private readonly authRepository: IAuthRepository;

  constructor(
    @inject(DI_TOKENS.IAuthRepository)
    authRepository: IAuthRepository,
  ) {
    this.authRepository = authRepository;
  }

  execute(credentials: LoginCredentials): Promise<LoginSession> {
    return this.authRepository.login(credentials);
  }
}
