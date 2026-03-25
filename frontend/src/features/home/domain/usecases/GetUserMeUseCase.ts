import type { IUserRepository } from '../repositories/IUserRepository';
import type { UserMe } from '../entities/UserMe';

export class GetUserMeUseCase {
  private readonly repo: IUserRepository;
  constructor(repo: IUserRepository) { this.repo = repo; }
  execute(): Promise<UserMe> {
    return this.repo.getMe();
  }
}
