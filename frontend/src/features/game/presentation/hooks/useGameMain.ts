import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import type { GameAssets } from '@features/game/domain/entities/GameAssets';
import type { GameStats } from '@features/game/domain/entities/GameStats';
import { useGameWorld } from '@features/game/presentation/hooks/useGameWorld';

interface LocationState {
  sessionId: number;
  openLoan?: boolean;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

export const useGameMain = () => {
  const location = useLocation();
  const { sessionId, openLoan, preSelectedPropertyId, preSelectedPropertyName, preSelectedPropertyPrice } =
    (location.state as LocationState) ?? { sessionId: null };

  const [assets, setAssets] = useState<GameAssets | null>(null);
  const [stats, setStats] = useState<GameStats | null>(null);
  const [characterType, setCharacterType] = useState<'MALE' | 'FEMALE'>('MALE');
  const [isLoading, setIsLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [leftView, setLeftView] = useState<'scene' | 'loan' | 'card'>(openLoan ? 'loan' : 'scene');

  const { turn, fetchTurn } = useGameWorld(sessionId);
  const currentDate = turn?.currentDate ?? null;

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      try {
        await fetchTurn();
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
        setCharacterType('MALE');
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, [sessionId]);

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
    isLoading,
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
