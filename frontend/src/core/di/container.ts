import { container } from 'tsyringe';
import { DI_TOKENS } from './tokens';
import { GameInitRemoteDataSource } from '@features/game/data/datasources/GameInitRemoteDataSource';
import { GameInitRepositoryImpl } from '@features/game/data/repositories/GameInitRepositoryImpl';
import { GameSessionRemoteDataSource } from '@features/game/data/datasources/GameSessionRemoteDataSource';
import { GameSessionRepositoryImpl } from '@features/game/data/repositories/GameSessionRepositoryImpl';
import { GameWorldRemoteDataSource } from '@features/game/data/datasources/GameWorldRemoteDataSource';
import { GameWorldRepositoryImpl } from '@features/game/data/repositories/GameWorldRepositoryImpl';
import { RealEstateRemoteDataSource } from '@features/realEstate/data/datasources/RealEstateRemoteDataSource';
import { RealEstateRepositoryImpl } from '@features/realEstate/data/repositories/RealEstateRepositoryImpl';

container.registerSingleton(GameInitRemoteDataSource);
container.register(DI_TOKENS.IGameInitRepository, {
  useClass: GameInitRepositoryImpl,
});
container.registerSingleton(GameSessionRemoteDataSource);
container.register(DI_TOKENS.IGameSessionRepository, {
  useClass: GameSessionRepositoryImpl,
});
container.registerSingleton(GameWorldRemoteDataSource);
container.register(DI_TOKENS.IGameWorldRepository, {
  useClass: GameWorldRepositoryImpl,
});
container.registerSingleton(RealEstateRemoteDataSource);
container.register(DI_TOKENS.IRealEstateRepository, {
  useClass: RealEstateRepositoryImpl,
});

export { container };
