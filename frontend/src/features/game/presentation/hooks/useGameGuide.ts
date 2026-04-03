import { createContext, useContext } from 'react';
import type { GameGuideControllerValue } from './useGameGuideController';

export const GameGuideContext = createContext<GameGuideControllerValue | null>(null);

export function useGameGuide(): GameGuideControllerValue {
  const context = useContext(GameGuideContext);

  if (context === null) {
    throw new Error('useGameGuide must be used within GameGuideLayout.');
  }

  return context;
}
