import type {
  GameTurn,
  NewsHistoryItem,
  PendingGameEvent,
  ResolvedGameEvent,
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
  getNewsHistory(sessionId: number): Promise<readonly NewsHistoryItem[]>
  getPendingEvents(sessionId: number): Promise<readonly PendingGameEvent[]>
  resolveEvent(
    sessionId: number,
    eventId: number,
    choiceId: number | null,
  ): Promise<ResolvedGameEvent>
  submitTurnSlots(
    sessionId: number,
    slots: readonly TurnSlotSelection[],
  ): Promise<TurnPreview>
  commitTurn(sessionId: number): Promise<TurnCommitResult>
}
