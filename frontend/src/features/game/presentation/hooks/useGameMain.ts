import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import type { JobType } from '@features/game/domain/entities/GameSlot';
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
  const [leftView, setLeftView] = useState<'scene' | 'loan' | 'card'>(openLoan ? 'loan' : 'scene');
  const createSessionPromiseRef = useRef<Promise<GameSessionCreation> | null>(null);

  const { createSession, isSubmitting, error: createError } = useCreateGameSession();
  const {
    turn,
    news,
    isTurnLoading,
    turnError,
    isNewsLoading,
    newsError,
    fetchTurn,
    fetchLatestNews,
  } = useGameTurn(sessionId);
  const currentDate = turn?.currentDate ?? null;
  const currentDateKey = currentDate === null ? null : formatIsoDate(currentDate);

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
    if (currentDateKey === null) {
      return;
    }

    if (readSessionStorage(getNewsSeenDateStorageKey(sessionId)) !== currentDateKey) {
      setIsNewsOpen(true);
    }
  }, [currentDateKey, sessionId]);

  useEffect(() => {
    if (!isNewsOpen) {
      return;
    }

    void fetchLatestNews();
  }, [fetchLatestNews, isNewsOpen]);

  return {
    sessionId,
    turn,
    news,
    currentDate,
    characterType,
    isNewsOpen,
    isLoading: isLoading || isSubmitting || isTurnLoading,
    isNewsLoading,
    newsError,
    openNews: () => setIsNewsOpen(true),
    closeNews: () => {
      writeSessionStorage(
        getNewsSeenDateStorageKey(sessionId),
        getNewsSeenDateValue(currentDate),
      );
      setIsNewsOpen(false);
    },
    error: error ?? createError ?? turnError,
    leftView,
    setLeftView,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  };
};
