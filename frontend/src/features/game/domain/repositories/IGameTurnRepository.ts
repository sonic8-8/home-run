import type {
  GameTurn,
  TurnCommitResult,
  TurnNews,
  TurnPreview,
  TurnSlotSelection,
} from '@features/game/domain/entities/GameTurn'
import type { TurnActions } from '@features/game/domain/entities/TurnAction'

export interface IGameTurnRepository {
  getTurn(sessionId: number): Promise<GameTurn>
  getAvailableActions(sessionId: number): Promise<TurnActions>
  getLatestNews(sessionId: number): Promise<TurnNews>
  submitTurnSlots(
    sessionId: number,
    slots: readonly TurnSlotSelection[],
  ): Promise<TurnPreview>
  commitTurn(sessionId: number): Promise<TurnCommitResult>
}
