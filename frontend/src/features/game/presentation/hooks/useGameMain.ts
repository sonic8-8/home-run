import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { CharacterType } from '@features/game/domain/entities/CharacterOption';
import type { GameAssets } from '@features/game/domain/entities/GameAssets';
import type { GameStats } from '@features/game/domain/entities/GameStats';
import type { JobType } from '@features/game/domain/entities/GameSlot';
import { useGameWorld } from '@features/game/presentation/hooks/useGameWorld';
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

const isValidSlotNumber = (
  slotNumber: number | undefined,
): slotNumber is 1 | 2 | 3 =>
  slotNumber === 1 || slotNumber === 2 || slotNumber === 3;

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

  const [assets, setAssets] = useState<GameAssets | null>(null);
  const [stats, setStats] = useState<GameStats | null>(null);
  const [sessionId, setSessionId] = useState<number | null>(locationSessionId ?? null);
  const [characterType, setCharacterType] = useState<CharacterType>(
    routeCharacterType ?? 'MALE',
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [leftView, setLeftView] = useState<'scene' | 'loan' | 'card'>(openLoan ? 'loan' : 'scene');
  const creationRequestedRef = useRef(false);

  const { createSession, isSubmitting, error: createError } = useCreateGameSession();
  const { turn, fetchTurn, error: worldError } = useGameWorld(sessionId);
  const currentDate = turn?.currentDate ?? null;

  useEffect(() => {
    if (locationSessionId === undefined || locationSessionId === sessionId) {
      return;
    }
    setSessionId(locationSessionId);
  }, [locationSessionId, sessionId]);

  useEffect(() => {
    let isMounted = true;

    if (sessionId !== null) {
      creationRequestedRef.current = true;
      return () => {
        isMounted = false;
      };
    }

    if (
      !isValidSlotNumber(slotNumber) ||
      routeCharacterType === undefined ||
      characterName === undefined ||
      jobType === undefined ||
      regionCode === undefined ||
      districtCode === undefined ||
      targetPropertyId === undefined ||
      useMyData === undefined
    ) {
      if (isMounted) {
        setError('세션 생성 정보가 부족합니다.');
      }
      return () => {
        isMounted = false;
      };
    }

    if (creationRequestedRef.current) {
      return () => {
        isMounted = false;
      };
    }

    creationRequestedRef.current = true;

    const createNewSession = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const createdSession = await createSession({
          slotNumber,
          characterType: routeCharacterType,
          characterName,
          jobType,
          regionCode,
          districtCode,
          targetPropertyId,
          useMyData,
        });

        if (!isMounted) {
          return;
        }

        setSessionId(createdSession.sessionId);
        navigate(ROUTES.GAME, {
          replace: true,
          state: {
            sessionId: createdSession.sessionId,
            characterType: routeCharacterType,
          },
        });
      } catch (sessionCreateError) {
        if (!isMounted) {
          return;
        }
        creationRequestedRef.current = false;
        setError(
          sessionCreateError instanceof Error
            ? sessionCreateError.message
            : '새 게임을 시작하지 못했습니다.',
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void createNewSession();

    return () => {
      isMounted = false;
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
        // TODO: GET /games/sessions/{sessionId}/assets
        setAssets({
          cash: 1_000_000,
          loan: { principal: 1_000_000, monthlyInterest: 30_000 },
          realEstate: {
            propertyName: '강남 힐스테이트 에코',
            housingType: 'JEONSE_APT',
            currentValue: 720_000_000,
          },
          stock: {
            totalValue: 100_000,
            holdings: [
              { stockCode: 'BIO', stockName: '바이오주', quantity: 11, currentValue: 100_000 },
            ],
          },
          career: {
            characterName: '김싸피',
            jobType: 'SMALL_BIZ',
            jobTitle: '스타트업 직장인',
            annualSalary: 36_000_000,
          },
          sideJobs: [
            { sideJobId: 1, name: '인형 눈 붙이기', cashEffect: 100_000, healthEffect: -5 },
            { sideJobId: 2, name: '배달 아르바이트', cashEffect: 300_000, healthEffect: -10 },
            { sideJobId: 3, name: '인형 눈 붙이기', cashEffect: 100_000, healthEffect: -5 },
          ],
        });
        // TODO: GET /games/sessions/{sessionId} (for stats & characterType)
        setStats({
          health: 30,
          fatigue: 45,
          stress: 15,
          knowledge: 70,
          happiness: 90,
        });
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

  const totalAssets =
    assets !== null
      ? assets.cash +
        (assets.realEstate?.currentValue ?? 0) +
        (assets.stock?.totalValue ?? 0) -
        (assets.loan?.principal ?? 0)
      : null;

  return {
    sessionId,
    assets,
    stats,
    turn,
    currentDate,
    characterType,
    totalAssets,
    isLoading: isLoading || isSubmitting,
    error: error ?? createError ?? worldError,
    isModalOpen,
    openModal: () => setIsModalOpen(true),
    closeModal: () => setIsModalOpen(false),
    leftView,
    setLeftView,
    preSelectedPropertyId,
    preSelectedPropertyName,
    preSelectedPropertyPrice,
  };
};
