import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import { isNetworkError, toErrorMessage } from '@core/error/AppError';
import { GetEndingLogsUseCase } from '@features/ending/domain/usecases/GetEndingLogsUseCase';
import { GetEndingReportUseCase } from '@features/ending/domain/usecases/GetEndingReportUseCase';
import type { EndingReport } from '@features/ending/domain/entities/EndingReport';
import type { EndingTimelinePoint } from '@features/ending/domain/entities/EndingTimeline';

export function useEndingPage() {
  const navigate = useNavigate();
  const { sessionId: sessionIdParam } = useParams<{ sessionId: string }>();
  const sessionId = sessionIdParam === undefined ? null : Number(sessionIdParam);
  const hasValidSessionId =
    sessionId !== null &&
    Number.isInteger(sessionId) &&
    sessionId > 0;

  const [report, setReport] = useState<EndingReport | null>(null);
  const [timeline, setTimeline] = useState<EndingTimelinePoint[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isNotReady, setIsNotReady] = useState(false);

  useEffect(() => {
    if (!hasValidSessionId) {
      navigate(ROUTES.GAME, { replace: true });
      return;
    }

    let isMounted = true;

    const fetchEndingPage = async () => {
      setIsLoading(true);
      setError(null);
      setIsNotReady(false);

      try {
        const getEndingReportUseCase = container.resolve(GetEndingReportUseCase);
        const getEndingLogsUseCase = container.resolve(GetEndingLogsUseCase);
        const [nextReport, nextTimeline] = await Promise.all([
          getEndingReportUseCase.execute(sessionId),
          getEndingLogsUseCase.execute(sessionId),
        ]);

        if (!isMounted) {
          return;
        }

        setReport(nextReport);
        setTimeline(nextTimeline);
      } catch (caught) {
        if (!isMounted) {
          return;
        }

        if (isNetworkError(caught) && caught.statusCode === 409) {
          setIsNotReady(true);
          return;
        }

        setError(toErrorMessage(caught));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void fetchEndingPage();

    return () => {
      isMounted = false;
    };
  }, [hasValidSessionId, navigate, sessionId]);

  return {
    report,
    timeline,
    isLoading,
    error,
    isNotReady,
    hasValidSessionId,
  };
}
