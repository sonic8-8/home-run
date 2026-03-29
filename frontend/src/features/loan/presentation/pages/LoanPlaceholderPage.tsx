import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import { GetGameSlotsUseCase } from '@features/game/domain/usecases/GetGameSlotsUseCase';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import styles from './LoanPlaceholderPage.module.css';

interface LoanPageLocationState {
  sessionId?: number;
}

function selectActiveSession(slots: GameSlot[]): number | null {
  const activeSlots = slots
    .filter((slot) => slot.status === 'IN_PROGRESS' && slot.sessionId !== null)
    .sort((left, right) => {
      if (left.createdAt && right.createdAt) {
        return right.createdAt.localeCompare(left.createdAt);
      }

      return left.slotNumber - right.slotNumber;
    });

  return activeSlots[0]?.sessionId ?? null;
}

export function LoanPlaceholderPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const locationState = (location.state as LoanPageLocationState | null) ?? null;
  const locationSessionId =
    locationState?.sessionId !== undefined &&
    Number.isInteger(locationState.sessionId) &&
    locationState.sessionId > 0
      ? locationState.sessionId
      : null;

  const [resolvedSessionId, setResolvedSessionId] = useState<number | null>(locationSessionId);
  const [isLoading, setIsLoading] = useState(locationSessionId === null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (locationSessionId !== null) {
      setResolvedSessionId(locationSessionId);
      setIsLoading(false);
      setError(null);
      return;
    }

    let isMounted = true;

    const resolveSession = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const useCase = container.resolve(GetGameSlotsUseCase);
        const slots = await useCase.execute();
        if (!isMounted) {
          return;
        }

        setResolvedSessionId(selectActiveSession(slots));
      } catch (resolveError) {
        if (!isMounted) {
          return;
        }

        setResolvedSessionId(null);
        setError(toErrorMessage(resolveError));
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void resolveSession();

    return () => {
      isMounted = false;
    };
  }, [locationSessionId]);

  const hasActiveSession = useMemo(() => resolvedSessionId !== null, [resolvedSessionId]);

  return (
    <div className={styles.page} data-testid="loan-page">
      <div className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Loan Center</p>
          <h1 className={styles.title}>대출 알아보기</h1>
          <p className={styles.description}>
            대출 상품 비교와 신청, 상환은 게임 진행 중 화면에서 바로 이어진다. 여기서는 진행 중인 세션으로
            바로 돌아가 대출 패널을 열 수 있다.
          </p>
        </div>
        <button
          type="button"
          className={styles.backButton}
          onClick={() => navigate(ROUTES.HOME)}
        >
          홈으로 돌아가기
        </button>
      </div>

      {isLoading ? (
        <div className={styles.stateCard} data-testid="loan-loading">
          진행 중인 세션을 확인하는 중입니다.
        </div>
      ) : error !== null ? (
        <div className={styles.stateCard} data-testid="loan-error" role="alert">
          <p className={styles.stateTitle}>대출 정보를 준비하지 못했습니다.</p>
          <p className={styles.stateDescription}>{error}</p>
          <button
            type="button"
            className={styles.primaryButton}
            onClick={() => navigate(ROUTES.GAME_START)}
          >
            게임 시작 화면으로
          </button>
        </div>
      ) : !hasActiveSession ? (
        <div className={styles.stateCard} data-testid="loan-no-session">
          <p className={styles.stateTitle}>진행 중인 게임 세션이 없습니다.</p>
          <p className={styles.stateDescription}>
            대출은 홈런 게임 진행 중인 세션 안에서 상품 비교, 심사 신청, 확정까지 이어서 진행할 수 있습니다.
          </p>
          <div className={styles.actionRow}>
            <button
              type="button"
              className={styles.primaryButton}
              onClick={() => navigate(ROUTES.GAME_START)}
            >
              게임 시작하기
            </button>
            <button
              type="button"
              className={styles.secondaryButton}
              onClick={() => navigate(ROUTES.GAME_SAVE_WITH_MODE('continue'))}
            >
              이어하기 선택
            </button>
          </div>
        </div>
      ) : resolvedSessionId !== null ? (
        <div className={styles.stateCard} data-testid="loan-active-session">
          <p className={styles.stateTitle}>진행 중인 세션이 준비되어 있습니다.</p>
          <p className={styles.stateDescription}>
            게임 화면으로 돌아가 `대출/상환` 패널에서 상품 비교, 심사 신청, 상환까지 이어서 진행해 주세요.
          </p>
          <div className={styles.actionRow}>
            <button
              type="button"
              className={styles.primaryButton}
              onClick={() =>
                navigate(ROUTES.GAME, {
                  state: {
                    sessionId: resolvedSessionId,
                    openLoan: true,
                  },
                })
              }
            >
              게임으로 돌아가기
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
