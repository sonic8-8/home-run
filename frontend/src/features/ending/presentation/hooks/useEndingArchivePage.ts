import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { GetGameSlotsUseCase } from '@features/game/domain/usecases/GetGameSlotsUseCase';

const ENDED_STATUSES = new Set(['CLEAR', 'BANKRUPT', 'TIMEOUT', 'FORECLOSURE']);

export function useEndingArchivePage() {
  const [endings, setEndings] = useState<GameSlot[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isMountedRef = useRef(true);

  const fetchEndings = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setError(null);

    try {
      const useCase = container.resolve(GetGameSlotsUseCase);
      const slots = await useCase.execute();

      if (!isMountedRef.current) {
        return;
      }

      const archivedEndings = slots
        .filter((slot) => slot.sessionId !== null && ENDED_STATUSES.has(slot.status))
        .sort((a, b) => {
          const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
          const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
          return bTime - aTime;
        });

      setEndings(archivedEndings);
    } catch (caught) {
      if (!isMountedRef.current) {
        return;
      }

      setEndings([]);
      setError(toErrorMessage(caught));
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    isMountedRef.current = true;
    void fetchEndings();

    return () => {
      isMountedRef.current = false;
    };
  }, [fetchEndings]);

  const isEmpty = useMemo(
    () => !isLoading && error === null && endings.length === 0,
    [endings.length, error, isLoading],
  );

  return {
    endings,
    isLoading,
    error,
    isEmpty,
    retryFetch: fetchEndings,
  };
}
