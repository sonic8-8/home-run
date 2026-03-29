import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import type { JobType } from '@features/game/domain/entities/GameSlot';
import type { PendingGameEvent } from '@features/game/domain/entities/GameTurn';
import { GetGameSessionDetailUseCase } from '@features/game/domain/usecases/GetGameSessionDetailUseCase';
import { useGameTurn } from '@features/game/presentation/hooks/useGameTurn';
import { formatIsoDate } from '@shared/utils/formatter';
import { readSessionStorage, writeSessionStorage } from '@shared/utils/sessionStorage';
import {
  getNewsSeenDateStorageKey,
  getNewsSeenDateValue,
} from '@features/game/utils/newsSeenStorage';
import type { GameSessionCreation } from '@features/game/domain/entities/GameSessionCreation';
import { useCreateGameSession } from './useCreateGameSession';

interface LocationState {
  sessionId?: number;
  slotNumber?: number;
  characterType?: CharacterType;
  characterName?: string;
  jobType?: JobType;
  regionCode?: string;
  districtCode?: string;
  targetPropertyId?: number;
  useMyData?: boolean;
  openLoan?: boolean;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

type NewSessionLocationState =
  | {
      slotNumber: 1 | 2 | 3;
      characterType: CharacterType;
      characterName: string;
      regionCode: string;
      districtCode: string;
      targetPropertyId: number;
      useMyData: true;
      jobType?: JobType;
    }
  | {
      slotNumber: 1 | 2 | 3;
      characterType: CharacterType;
      characterName: string;
      regionCode: string;
      districtCode: string;
      targetPropertyId: number;
      useMyData: false;
      jobType: JobType;
    };

const isValidSlotNumber = (
  slotNumber: number | undefined,
): slotNumber is 1 | 2 | 3 =>
  slotNumber === 1 || slotNumber === 2 || slotNumber === 3;

const isCompleteNewSessionState = (
  state: LocationState,
): state is NewSessionLocationState =>
  isValidSlotNumber(state.slotNumber) &&
  state.characterType !== undefined &&
  state.characterName !== undefined &&
  state.regionCode !== undefined &&
  state.districtCode !== undefined &&
  state.targetPropertyId !== undefined &&
  state.useMyData !== undefined &&
  (state.useMyData || state.jobType !== undefined);

export const useGameMain = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as LocationState) ?? {};
  const {
    sessionId: locationSessionId,
    slotNumber,
    characterType: routeCharacterType,
    characterName,
    jobType,
    regionCode,
    districtCode,
    targetPropertyId,
    useMyData,
    openLoan,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  } = state;
  const [sessionId, setSessionId] = useState<number | null>(locationSessionId ?? null);
  const [characterType, setCharacterType] = useState<CharacterType>(
    routeCharacterType ?? 'MALE',
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isNewsOpen, setIsNewsOpen] = useState(false);
  const [isMonthlyActivityOpen, setIsMonthlyActivityOpen] = useState(false);
  const [isGameEventOpen, setIsGameEventOpen] = useState(false);
  const [leftView, setLeftView] = useState<'scene' | 'loan' | 'card'>(openLoan ? 'loan' : 'scene');
  const createSessionPromiseRef = useRef<Promise<GameSessionCreation> | null>(null);
  const endingRedirectedRef = useRef(false);

  const { createSession, isSubmitting, error: createError } = useCreateGameSession();
  const {
    turn,
    news,
    pendingEvents,
    resolvedEvent,
    turnActions,
    turnPreview,
    turnCommitResult,
    isTurnLoading,
    turnError,
    isNewsLoading,
    newsError,
    isActionsLoading,
    isSlotSubmitting,
    isTurnCommitting,
    isPendingEventsLoading,
    isEventResolving,
    eventError,
    scheduleError,
    fetchTurn,
    fetchLatestNews,
    fetchPendingEvents,
    fetchTurnActions,
    resolvePendingEvent,
    submitTurnSlots,
    commitTurn,
    dismissResolvedEvent,
    resetPendingEventFlow,
    resetScheduleFlow,
  } = useGameTurn(sessionId);
  const currentDate = turn?.currentDate ?? null;
  const currentDateKey = currentDate === null ? null : formatIsoDate(currentDate);
  const currentPendingEvent: PendingGameEvent | null = pendingEvents.at(0) ?? null;

  useEffect(() => {
    if (locationSessionId === undefined || locationSessionId === sessionId) {
      return;
    }
    setSessionId(locationSessionId);
  }, [locationSessionId, sessionId]);

  useEffect(() => {
    let isCancelled = false;

    if (sessionId !== null) {
      return () => {
        isCancelled = true;
      };
    }

    const createState: LocationState = {
      slotNumber,
      characterType: routeCharacterType,
      characterName,
      jobType,
      regionCode,
      districtCode,
      targetPropertyId,
      useMyData,
    };

    if (!isCompleteNewSessionState(createState)) {
      if (!isCancelled) {
        setError('세션 생성 정보가 부족합니다.');
      }
      return () => {
        isCancelled = true;
      };
    }

    const createPromise =
      createSessionPromiseRef.current ??
      createSession(
        createState.useMyData
          ? {
              slotNumber: createState.slotNumber,
              characterType: createState.characterType,
              characterName: createState.characterName,
              regionCode: createState.regionCode,
              districtCode: createState.districtCode,
              targetPropertyId: createState.targetPropertyId,
              useMyData: createState.useMyData,
            }
          : {
              slotNumber: createState.slotNumber,
              characterType: createState.characterType,
              characterName: createState.characterName,
              jobType: createState.jobType,
              regionCode: createState.regionCode,
              districtCode: createState.districtCode,
              targetPropertyId: createState.targetPropertyId,
              useMyData: createState.useMyData,
            },
      );
    createSessionPromiseRef.current = createPromise;

    const createNewSession = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const createdSession = await createPromise;

        if (isCancelled) {
          return;
        }

        setSessionId(createdSession.sessionId);
        navigate(ROUTES.GAME, {
          replace: true,
          state: {
            sessionId: createdSession.sessionId,
            characterType: createState.characterType,
          },
        });
      } catch (sessionCreateError) {
        if (isCancelled) {
          return;
        }
        setError(
          sessionCreateError instanceof Error
            ? sessionCreateError.message
            : '새 게임을 시작하지 못했습니다.',
        );
      } finally {
        if (createSessionPromiseRef.current === createPromise) {
          createSessionPromiseRef.current = null;
        }

        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    void createNewSession();

    return () => {
      isCancelled = true;
    };
  }, [
    characterName,
    createSession,
    districtCode,
    jobType,
    navigate,
    regionCode,
    routeCharacterType,
    sessionId,
    slotNumber,
    targetPropertyId,
    useMyData,
  ]);

  useEffect(() => {
    let isMounted = true;

    if (sessionId === null) {
      return () => {
        isMounted = false;
      };
    }

    const load = async () => {
      setIsLoading(true);
      setError(null);
      try {
        await fetchTurn();
        if (!isMounted) {
          return;
        }
        setCharacterType(routeCharacterType ?? 'MALE');
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };
    void load();

    return () => {
      isMounted = false;
    };
  }, [fetchTurn, routeCharacterType, sessionId]);

  useEffect(() => {
    if (sessionId === null || endingRedirectedRef.current) {
      return;
    }

    let isMounted = true;

    const checkEndingStatus = async () => {
      try {
        const getGameSessionDetailUseCase = container.resolve(GetGameSessionDetailUseCase);
        const detail = await getGameSessionDetailUseCase.execute(sessionId);

        if (!isMounted || detail.status === 'IN_PROGRESS') {
          return;
        }

        endingRedirectedRef.current = true;
        navigate(ROUTES.GAME_ENDING(sessionId), { replace: true });
      } catch {
        // Ignore transient detail fetch errors and keep the player on the main screen.
      }
    };

    void checkEndingStatus();

    return () => {
      isMounted = false;
    };
  }, [currentDateKey, navigate, sessionId]);

  useEffect(() => {
    if (currentDateKey === null) {
      return;
    }

    if (isMonthlyActivityOpen) {
      return;
    }

    if (readSessionStorage(getNewsSeenDateStorageKey(sessionId)) !== currentDateKey) {
      setIsNewsOpen(true);
    }
  }, [currentDateKey, isMonthlyActivityOpen, sessionId]);

  useEffect(() => {
    if (!isNewsOpen) {
      return;
    }

    void fetchLatestNews();
  }, [fetchLatestNews, isNewsOpen]);

  const openMonthlyActivity = useCallback(() => {
    if (sessionId === null) {
      return;
    }

    setIsMonthlyActivityOpen(true);
    void fetchTurnActions();
  }, [fetchTurnActions, sessionId]);

  const closeMonthlyActivity = useCallback(() => {
    setIsMonthlyActivityOpen(false);
    resetScheduleFlow();
  }, [resetScheduleFlow]);

  const closeGameEvent = useCallback(() => {
    setIsGameEventOpen(false);
    resetPendingEventFlow();
  }, [resetPendingEventFlow]);

  const handleSubmitTurnSlots = useCallback(
    async (actionTypes: readonly string[]) => submitTurnSlots(actionTypes),
    [submitTurnSlots],
  );

  const handleCommitTurn = useCallback(async () => {
    const result = await commitTurn();

    if (result === null) {
      return null;
    }

    await fetchTurn();
    return result;
  }, [commitTurn, fetchTurn]);

  const handleConfirmCommitResult = useCallback(async () => {
    if (turnCommitResult?.flags.hasEvent !== true) {
      closeMonthlyActivity();
      return;
    }

    resetPendingEventFlow();
    const events = await fetchPendingEvents();

    if (events === null) {
      return;
    }

    closeMonthlyActivity();
    if (events.length > 0) {
      setIsGameEventOpen(true);
    }
  }, [
    closeMonthlyActivity,
    fetchPendingEvents,
    resetPendingEventFlow,
    turnCommitResult,
  ]);

  const handleResolveGameEvent = useCallback(async (choiceId: number | null) => {
    if (currentPendingEvent === null) {
      return null;
    }

    return resolvePendingEvent(currentPendingEvent.eventId, choiceId);
  }, [currentPendingEvent, resolvePendingEvent]);

  const handleAdvanceGameEvent = useCallback(() => {
    dismissResolvedEvent();

    if (pendingEvents.length === 0) {
      closeGameEvent();
    }
  }, [closeGameEvent, dismissResolvedEvent, pendingEvents.length]);

  return {
    sessionId,
    turn,
    news,
    currentPendingEvent,
    resolvedEvent,
    hasMorePendingEvents: pendingEvents.length > 0,
    turnActions,
    turnPreview,
    turnCommitResult,
    currentDate,
    characterType,
    isNewsOpen,
    isLoading: isLoading || isSubmitting || isTurnLoading,
    isNewsLoading,
    newsError,
    isMonthlyActivityOpen,
    isActionsLoading,
    isSlotSubmitting,
    isTurnCommitting,
    isGameEventOpen,
    isPendingEventsLoading,
    isEventResolving,
    scheduleError,
    eventError,
    openNews: () => setIsNewsOpen(true),
    closeNews: () => {
      writeSessionStorage(
        getNewsSeenDateStorageKey(sessionId),
        getNewsSeenDateValue(currentDate),
      );
      setIsNewsOpen(false);
    },
    openMonthlyActivity,
    closeMonthlyActivity,
    closeGameEvent,
    submitTurnSlots: handleSubmitTurnSlots,
    commitTurn: handleCommitTurn,
    confirmCommitResult: handleConfirmCommitResult,
    resolveGameEvent: handleResolveGameEvent,
    advanceGameEvent: handleAdvanceGameEvent,
    resetScheduleFlow,
    error: error ?? createError ?? turnError,
    leftView,
    setLeftView,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  };
};
