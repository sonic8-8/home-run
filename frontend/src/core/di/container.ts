import { container } from 'tsyringe';
import { DI_TOKENS } from './tokens';
import { EndingRemoteDataSource } from '@features/ending/data/datasources/EndingRemoteDataSource';
import { EndingRepositoryImpl } from '@features/ending/data/repositories/EndingRepositoryImpl';
import { AuthRemoteDataSource } from '@features/auth/data/datasources/AuthRemoteDataSource';
import { AuthRepositoryImpl } from '@features/auth/data/repositories/AuthRepositoryImpl';
import { LoanRemoteDataSource } from '@features/loan/data/datasources/LoanRemoteDataSource';
import { LoanRepositoryImpl } from '@features/loan/data/repositories/LoanRepositoryImpl';
import { GameInitRemoteDataSource } from '@features/game/data/datasources/GameInitRemoteDataSource';
import { GameInitRepositoryImpl } from '@features/game/data/repositories/GameInitRepositoryImpl';
import { GameSessionRemoteDataSource } from '@features/game/data/datasources/GameSessionRemoteDataSource';
import { GameSessionRepositoryImpl } from '@features/game/data/repositories/GameSessionRepositoryImpl';
import { GameTurnRemoteDataSource } from '@features/game/data/datasources/GameTurnRemoteDataSource';
import { GameTurnRepositoryImpl } from '@features/game/data/repositories/GameTurnRepositoryImpl';
import { RealEstateRemoteDataSource } from '@features/realEstate/data/datasources/RealEstateRemoteDataSource';
import { RealEstateRepositoryImpl } from '@features/realEstate/data/repositories/RealEstateRepositoryImpl';

container.registerSingleton(AuthRemoteDataSource);
container.register(DI_TOKENS.IAuthRepository, {
  useClass: AuthRepositoryImpl,
});
container.registerSingleton(EndingRemoteDataSource);
container.register(DI_TOKENS.IEndingRepository, {
  useClass: EndingRepositoryImpl,
});
container.registerSingleton(LoanRemoteDataSource);
container.register(DI_TOKENS.ILoanRepository, {
  useClass: LoanRepositoryImpl,
});
container.registerSingleton(GameInitRemoteDataSource);
container.register(DI_TOKENS.IGameInitRepository, {
  useClass: GameInitRepositoryImpl,
});
container.registerSingleton(GameSessionRemoteDataSource);
container.register(DI_TOKENS.IGameSessionRepository, {
  useClass: GameSessionRepositoryImpl,
});
container.registerSingleton(GameTurnRemoteDataSource);
container.register(DI_TOKENS.IGameTurnRepository, {
  useClass: GameTurnRepositoryImpl,
});
container.registerSingleton(RealEstateRemoteDataSource);
container.register(DI_TOKENS.IRealEstateRepository, {
  useClass: RealEstateRepositoryImpl,
});

export { container };
