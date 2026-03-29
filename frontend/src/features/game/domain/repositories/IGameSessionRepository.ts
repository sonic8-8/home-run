import type { CreateGameSessionInput } from '../entities/CreateGameSessionInput';
import type { GameSessionCreation } from '../entities/GameSessionCreation';
import type { GameSessionDetail, GameSlot } from '../entities/GameSlot';

export interface IGameSessionRepository {
  createSession(input: CreateGameSessionInput): Promise<GameSessionCreation>;
  getSlots(): Promise<GameSlot[]>;
  getSessionDetail(sessionId: number): Promise<GameSessionDetail>;
}
