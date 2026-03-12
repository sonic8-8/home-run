import { useState, useCallback } from 'react';
import type { GameEvent, GameEventId } from '@features/game/domain/entities/GameEvent';
import { GAME_EVENTS } from '@features/game/domain/constants/gameEvents';

interface UseGameEventReturn {
  currentEvent: GameEvent | null;
  showEvent: (eventId: GameEventId) => void;
  hideEvent: () => void;
  handleAction: (actionId: string) => void;
}

export const useGameEvent = (): UseGameEventReturn => {
  const [currentEvent, setCurrentEvent] = useState<GameEvent | null>(null);

  const showEvent = useCallback((eventId: GameEventId) => {
    setCurrentEvent(GAME_EVENTS[eventId]);
  }, []);

  const hideEvent = useCallback(() => {
    setCurrentEvent(null);
  }, []);

  const handleAction = useCallback((actionId: string) => {
    switch (actionId) {
      case 'RECEIVE':
        // TODO: 선물/급여 수령 처리 (UseCase 연결)
        break;
      case 'APPLY':
        // TODO: 지원금 신청 처리 (UseCase 연결)
        break;
      case 'REJECT':
        // TODO: 거절 처리 (UseCase 연결)
        break;
      case 'CONFIRM':
        // TODO: 확인 처리 (UseCase 연결)
        break;
      case 'PAY':
        // TODO: 납부 처리 (UseCase 연결)
        break;
    }
    hideEvent();
  }, [hideEvent]);

  return { currentEvent, showEvent, hideEvent, handleAction };
};
