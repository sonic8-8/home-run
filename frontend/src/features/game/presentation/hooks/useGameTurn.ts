import { useState, useCallback } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { GetGameTurnUseCase } from '@features/game/domain/usecases/GetGameTurnUseCase';
import { GetLatestNewsUseCase } from '@features/game/domain/usecases/GetLatestNewsUseCase';
import type { GameTurn, TurnNews } from '@features/game/domain/entities/GameTurn';

export function useGameTurn(sessionId: number | null) {
  const [turn, setTurn] = useState<GameTurn | null>(null);
  const [news, setNews] = useState<TurnNews | null>(null);
  const [isTurnLoading, setIsTurnLoading] = useState(false);
  const [turnError, setTurnError] = useState<string | null>(null);
  const [isNewsLoading, setIsNewsLoading] = useState(false);
  const [newsError, setNewsError] = useState<string | null>(null);

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

  return {
    turn,
    news,
    isTurnLoading,
    turnError,
    isNewsLoading,
    newsError,
    fetchTurn,
    fetchLatestNews,
  };
}
