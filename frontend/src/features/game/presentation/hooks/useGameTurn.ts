import { useCallback, useEffect, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { GetNewsHistoryUseCase } from '@features/game/domain/usecases/GetNewsHistoryUseCase';
import { GetPendingEventsUseCase } from '@features/game/domain/usecases/GetPendingEventsUseCase';
import { GetGameTurnUseCase } from '@features/game/domain/usecases/GetGameTurnUseCase';
import { GetLatestNewsUseCase } from '@features/game/domain/usecases/GetLatestNewsUseCase';
import { GetTurnActionsUseCase } from '@features/game/domain/usecases/GetTurnActionsUseCase';
import { ResolveGameEventUseCase } from '@features/game/domain/usecases/ResolveGameEventUseCase';
import { SubmitTurnSlotsUseCase } from '@features/game/domain/usecases/SubmitTurnSlotsUseCase';
import { CommitTurnUseCase } from '@features/game/domain/usecases/CommitTurnUseCase';
import type {
  GameTurn,
  NewsHistoryItem,
  PendingGameEvent,
  ResolvedGameEvent,
  TurnCommitResult,
  TurnNews,
  TurnPreview,
} from '@features/game/domain/entities/GameTurn';
import type { TurnActions } from '@features/game/domain/entities/TurnAction';

export function useGameTurn(sessionId: number | null) {
  const [turn, setTurn] = useState<GameTurn | null>(null);
  const [news, setNews] = useState<TurnNews | null>(null);
  const [newsHistory, setNewsHistory] = useState<readonly NewsHistoryItem[]>([]);
  const [pendingEvents, setPendingEvents] = useState<readonly PendingGameEvent[]>([]);
  const [resolvedEvent, setResolvedEvent] = useState<ResolvedGameEvent | null>(null);
  const [turnActions, setTurnActions] = useState<TurnActions | null>(null);
  const [turnPreview, setTurnPreview] = useState<TurnPreview | null>(null);
  const [turnCommitResult, setTurnCommitResult] = useState<TurnCommitResult | null>(null);
  const [isTurnLoading, setIsTurnLoading] = useState(false);
  const [turnError, setTurnError] = useState<string | null>(null);
  const [isNewsLoading, setIsNewsLoading] = useState(false);
  const [newsError, setNewsError] = useState<string | null>(null);
  const [isNewsHistoryLoading, setIsNewsHistoryLoading] = useState(false);
  const [newsHistoryError, setNewsHistoryError] = useState<string | null>(null);
  const [isActionsLoading, setIsActionsLoading] = useState(false);
  const [isSlotSubmitting, setIsSlotSubmitting] = useState(false);
  const [isTurnCommitting, setIsTurnCommitting] = useState(false);
  const [isPendingEventsLoading, setIsPendingEventsLoading] = useState(false);
  const [isEventResolving, setIsEventResolving] = useState(false);
  const [eventError, setEventError] = useState<string | null>(null);
  const [scheduleError, setScheduleError] = useState<string | null>(null);

  useEffect(() => {
    setPendingEvents([]);
    setResolvedEvent(null);
    setEventError(null);
    setNewsHistory([]);
    setNewsHistoryError(null);
  }, [sessionId]);

  const fetchTurn = useCallback(async (): Promise<GameTurn | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsTurnLoading(true);
    setTurnError(null);

    try {
      const getTurnUseCase = container.resolve(GetGameTurnUseCase);
      const result = await getTurnUseCase.execute(sessionId);
      setTurn(result);
      return result;
    } catch (error) {
      setTurnError(toErrorMessage(error));
      return null;
    } finally {
      setIsTurnLoading(false);
    }
  }, [sessionId]);

  const fetchLatestNews = useCallback(async (): Promise<TurnNews | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsNewsLoading(true);
    setNewsError(null);

    try {
      const getLatestNewsUseCase = container.resolve(GetLatestNewsUseCase);
      const result = await getLatestNewsUseCase.execute(sessionId);
      setNews(result);
      return result;
    } catch (error) {
      setNewsError(toErrorMessage(error));
      return null;
    } finally {
      setIsNewsLoading(false);
    }
  }, [sessionId]);

  const fetchNewsHistory = useCallback(async (): Promise<readonly NewsHistoryItem[] | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsNewsHistoryLoading(true);
    setNewsHistoryError(null);

    try {
      const getNewsHistoryUseCase = container.resolve(GetNewsHistoryUseCase);
      const result = await getNewsHistoryUseCase.execute(sessionId);
      setNewsHistory(result);
      return result;
    } catch (error) {
      setNewsHistory([]);
      setNewsHistoryError(toErrorMessage(error));
      return null;
    } finally {
      setIsNewsHistoryLoading(false);
    }
  }, [sessionId]);

  const fetchPendingEvents = useCallback(async (): Promise<readonly PendingGameEvent[] | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsPendingEventsLoading(true);
    setEventError(null);
    setResolvedEvent(null);

    try {
      const getPendingEventsUseCase = container.resolve(GetPendingEventsUseCase);
      const result = await getPendingEventsUseCase.execute(sessionId);
      setPendingEvents(result);
      return result;
    } catch (error) {
      setPendingEvents([]);
      setEventError(toErrorMessage(error));
      return null;
    } finally {
      setIsPendingEventsLoading(false);
    }
  }, [sessionId]);

  const resolvePendingEvent = useCallback(async (
    eventId: number,
    choiceId: number | null,
  ): Promise<ResolvedGameEvent | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsEventResolving(true);
    setEventError(null);

    try {
      const resolveGameEventUseCase = container.resolve(ResolveGameEventUseCase);
      const result = await resolveGameEventUseCase.execute(sessionId, eventId, choiceId);
      setPendingEvents((prev) => prev.filter((event) => event.eventId !== eventId));
      setResolvedEvent(result);
      return result;
    } catch (error) {
      setResolvedEvent(null);
      setEventError(toErrorMessage(error));
      return null;
    } finally {
      setIsEventResolving(false);
    }
  }, [sessionId]);

  const fetchTurnActions = useCallback(async (): Promise<TurnActions | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsActionsLoading(true);
    setScheduleError(null);
    setTurnPreview(null);
    setTurnCommitResult(null);

    try {
      const getTurnActionsUseCase = container.resolve(GetTurnActionsUseCase);
      const result = await getTurnActionsUseCase.execute(sessionId);
      setTurnActions(result);
      return result;
    } catch (error) {
      setTurnActions(null);
      setScheduleError(toErrorMessage(error));
      return null;
    } finally {
      setIsActionsLoading(false);
    }
  }, [sessionId]);

  const submitTurnSlots = useCallback(async (
    actionTypes: readonly string[],
  ): Promise<TurnPreview | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsSlotSubmitting(true);
    setScheduleError(null);

    try {
      const submitTurnSlotsUseCase = container.resolve(SubmitTurnSlotsUseCase);
      const result = await submitTurnSlotsUseCase.execute(
        sessionId,
        actionTypes.map((actionType, slotIndex) => ({
          slotIndex,
          actionType,
        })),
      );
      setTurnPreview(result);
      setTurnCommitResult(null);
      return result;
    } catch (error) {
      setTurnPreview(null);
      setScheduleError(toErrorMessage(error));
      return null;
    } finally {
      setIsSlotSubmitting(false);
    }
  }, [sessionId]);

  const commitTurn = useCallback(async (): Promise<TurnCommitResult | null> => {
    if (sessionId === null) {
      return null;
    }

    setIsTurnCommitting(true);
    setScheduleError(null);

    try {
      const commitTurnUseCase = container.resolve(CommitTurnUseCase);
      const result = await commitTurnUseCase.execute(sessionId);
      setTurnCommitResult(result);
      return result;
    } catch (error) {
      setTurnCommitResult(null);
      setScheduleError(toErrorMessage(error));
      return null;
    } finally {
      setIsTurnCommitting(false);
    }
  }, [sessionId]);

  const resetScheduleFlow = useCallback(() => {
    setTurnPreview(null);
    setTurnCommitResult(null);
    setScheduleError(null);
  }, []);

  const dismissResolvedEvent = useCallback(() => {
    setResolvedEvent(null);
    setEventError(null);
  }, []);

  const resetPendingEventFlow = useCallback(() => {
    setPendingEvents([]);
    setResolvedEvent(null);
    setEventError(null);
  }, []);

  return {
    turn,
    news,
    newsHistory,
    pendingEvents,
    resolvedEvent,
    turnActions,
    turnPreview,
    turnCommitResult,
    isTurnLoading,
    turnError,
    isNewsLoading,
    newsError,
    isNewsHistoryLoading,
    newsHistoryError,
    isActionsLoading,
    isSlotSubmitting,
    isTurnCommitting,
    isPendingEventsLoading,
    isEventResolving,
    eventError,
    scheduleError,
    fetchTurn,
    fetchLatestNews,
    fetchNewsHistory,
    fetchPendingEvents,
    fetchTurnActions,
    resolvePendingEvent,
    submitTurnSlots,
    commitTurn,
    dismissResolvedEvent,
    resetPendingEventFlow,
    resetScheduleFlow,
  };
}
