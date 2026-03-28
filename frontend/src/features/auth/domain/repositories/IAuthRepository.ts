import type { LoginCredentials } from '../entities/LoginCredentials';
import type { LoginSession } from '../entities/LoginSession';
import type { SignUpCredentials } from '../entities/SignUpCredentials';

export interface IAuthRepository {
  login(credentials: LoginCredentials): Promise<LoginSession>;
  signUp(credentials: SignUpCredentials): Promise<void>;
}
