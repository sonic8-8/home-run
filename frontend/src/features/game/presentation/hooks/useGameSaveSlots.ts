import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { ROUTES } from '@app/routes';

// Mock data — TODO: container.resolve(GetGameSlotsUseCase).execute() 로 교체
const MOCK_SLOTS: GameSlot[] = [
  {
    slotNumber: 1,
    sessionId: 1,
    status: 'IN_PROGRESS',
    characterName: '김싸피',
    jobType: 'LARGE_BIZ',
    totalAssets: 1_000_000,
    createdAt: '2026-03-06',
    currentTurn: 12,
  },
  {
    slotNumber: 2,
    sessionId: 2,
    status: 'IN_PROGRESS',
    characterName: '김싸피',
    jobType: 'FREELANCER',
    totalAssets: 1_000,
    createdAt: '2026-03-06',
    currentTurn: 3,
  },
  {
    slotNumber: 3,
    sessionId: null,
    status: 'EMPTY',
  },
];

export const useGameSaveSlots = () => {
  const [slots, setSlots] = useState<GameSlot[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchSlots = async () => {
      setIsLoading(true);
      setError(null);
      try {
        // TODO: const useCase = container.resolve(GetGameSlotsUseCase);
        // TODO: const result = await useCase.execute();
        // setSlots(result);
        setSlots(MOCK_SLOTS);
      } catch {
        setError('세이브 데이터를 불러오지 못했습니다.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchSlots();
  }, []);

  const handleSelectSlot = useCallback(
    (slot: GameSlot) => {
      if (slot.status === 'EMPTY') return;
      navigate(ROUTES.GAME, { state: { sessionId: slot.sessionId } });
    },
    [navigate],
  );

  return { slots, isLoading, error, handleSelectSlot };
};
