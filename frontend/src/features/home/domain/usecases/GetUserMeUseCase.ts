import type { IUserRepository } from '../repositories/IUserRepository';
import type { UserMe } from '../entities/UserMe';

export class GetUserMeUseCase {
  constructor(private readonly repo: IUserRepository) {}
  execute(): Promise<UserMe> {
    return this.repo.getMe();
  }
}
