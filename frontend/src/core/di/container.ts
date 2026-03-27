import { container } from 'tsyringe';
import { DI_TOKENS } from './tokens';
import { GameSessionRemoteDataSource } from '@features/game/data/datasources/GameSessionRemoteDataSource';
import { GameSessionRepositoryImpl } from '@features/game/data/repositories/GameSessionRepositoryImpl';

container.registerSingleton(GameSessionRemoteDataSource);
container.register(DI_TOKENS.IGameSessionRepository, {
  useClass: GameSessionRepositoryImpl,
});

export { container };
