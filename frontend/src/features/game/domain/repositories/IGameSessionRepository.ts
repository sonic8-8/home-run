import type { CreateGameSessionInput } from '../entities/CreateGameSessionInput';
import type { GameSessionCreation } from '../entities/GameSessionCreation';
import type { GameSlot } from '../entities/GameSlot';

export interface IGameSessionRepository {
  createSession(input: CreateGameSessionInput): Promise<GameSessionCreation>;
  getSlots(): Promise<GameSlot[]>;
}
