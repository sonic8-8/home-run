import { inject, injectable } from 'tsyringe';
import { DI_TOKENS } from '@core/di/tokens';
import type { SignUpCredentials } from '../entities/SignUpCredentials';
import type { IAuthRepository } from '../repositories/IAuthRepository';

@injectable()
export class SignUpUseCase {
  private readonly authRepository: IAuthRepository;

  constructor(
    @inject(DI_TOKENS.IAuthRepository)
    authRepository: IAuthRepository,
  ) {
    this.authRepository = authRepository;
  }

  execute(credentials: SignUpCredentials): Promise<void> {
    return this.authRepository.signUp(credentials);
  }
}
