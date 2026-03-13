import type { GameSlot } from '../entities/GameSlot';

export interface IGameSessionRepository {
  getSlots(): Promise<GameSlot[]>;
}
