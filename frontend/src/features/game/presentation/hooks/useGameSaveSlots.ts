import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { GetGameSlotsUseCase } from '@features/game/domain/usecases/GetGameSlotsUseCase';
import {
  getSlotUnavailableMessage,
  parseGameSaveEntryMode,
} from '@features/game/presentation/utils/saveSlotEntry';

export const useGameSaveSlots = () => {
  const [slots, setSlots] = useState<GameSlot[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isMountedRef = useRef(true);
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const queryEntryMode = parseGameSaveEntryMode(searchParams.get('mode'));
  const stateEntryMode = parseGameSaveEntryMode(location.state as {
    entryMode?: 'continue' | 'new';
  } | null);
  const entryMode = queryEntryMode ?? stateEntryMode;

  const fetchSlots = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setError(null);
    try {
      const useCase = container.resolve(GetGameSlotsUseCase);
      const result = await useCase.execute();
      if (!isMountedRef.current) {
        return;
      }
      setSlots(result);
    } catch (fetchError) {
      if (!isMountedRef.current) {
        return;
      }
      setSlots([]);
      setError(toErrorMessage(fetchError));
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    isMountedRef.current = true;
    void fetchSlots();
    return () => {
      isMountedRef.current = false;
    };
  }, [fetchSlots]);

  const handleSelectSlot = (slot: GameSlot) => {
    setError(null);

    if (entryMode === 'continue' || entryMode === 'new') {
      if (entryMode === 'continue') {
        if (slot.status !== 'IN_PROGRESS') {
          setError(getSlotUnavailableMessage(entryMode, slot));
          return;
        }

        if (slot.sessionId === null) {
          setError('세션 정보를 확인하지 못했습니다.');
          return;
        }

        navigate(ROUTES.GAME, { state: { sessionId: slot.sessionId } });
        return;
      }

      if (slot.status !== 'EMPTY') {
        setError(getSlotUnavailableMessage(entryMode, slot));
        return;
      }

      navigate(ROUTES.GAME_SELECT_CHARACTER, {
        state: { slotNumber: slot.slotNumber },
      });
      return;
    }

    if (slot.status === 'EMPTY') {
      navigate(ROUTES.GAME_SELECT_CHARACTER, {
        state: { slotNumber: slot.slotNumber },
      });
      return;
    }

    if (slot.status !== 'IN_PROGRESS') {
      setError('종료된 세션은 아직 이어하기를 지원하지 않습니다.');
      return;
    }

    if (slot.sessionId === null) {
      setError('세션 정보를 확인하지 못했습니다.');
      return;
    }

    navigate(ROUTES.GAME, { state: { sessionId: slot.sessionId } });
  };

  return {
    slots,
    isLoading,
    error,
    entryMode,
    retryFetchSlots: fetchSlots,
    handleSelectSlot,
  };
};
