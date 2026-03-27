import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { GameSlot } from '@features/game/domain/entities/GameSlot';
import { ROUTES } from '@app/routes';
import { container } from '@core/di/container';
import { GetGameSlotsUseCase } from '@features/game/domain/usecases/GetGameSlotsUseCase';

export const useGameSaveSlots = () => {
  const [slots, setSlots] = useState<GameSlot[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;

    const fetchSlots = async (): Promise<void> => {
      setIsLoading(true);
      setError(null);
      try {
        const useCase = container.resolve(GetGameSlotsUseCase);
        const result = await useCase.execute();
        if (!isMounted) {
          return;
        }
        setSlots(result);
      } catch (fetchError) {
        if (!isMounted) {
          return;
        }
        setSlots([]);
        setError(
          fetchError instanceof Error
            ? fetchError.message
            : '세이브 데이터를 불러오지 못했습니다.',
        );
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void fetchSlots();

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSelectSlot = (slot: GameSlot) => {
    setError(null);

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

  return { slots, isLoading, error, handleSelectSlot };
};
