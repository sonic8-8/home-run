import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { useGameTurn } from '@features/game/presentation/hooks/useGameTurn';

export function useGameNewsPage() {
  const navigate = useNavigate();
  const { sessionId: sessionIdParam } = useParams<{ sessionId: string }>();
  const sessionId = sessionIdParam === undefined ? null : Number(sessionIdParam);
  const hasValidSessionId =
    sessionId !== null &&
    Number.isInteger(sessionId) &&
    sessionId > 0;

  const {
    news,
    newsHistory,
    isNewsLoading,
    newsError,
    isNewsHistoryLoading,
    newsHistoryError,
    fetchLatestNews,
    fetchNewsHistory,
  } = useGameTurn(hasValidSessionId ? sessionId : null);

  useEffect(() => {
    if (!hasValidSessionId) {
      navigate(ROUTES.GAME, { replace: true });
      return;
    }

    void Promise.all([fetchLatestNews(), fetchNewsHistory()]);
  }, [fetchLatestNews, fetchNewsHistory, hasValidSessionId, navigate]);

  return {
    news,
    newsHistory,
    isNewsLoading,
    newsError,
    isNewsHistoryLoading,
    newsHistoryError,
    hasValidSessionId,
  };
}
