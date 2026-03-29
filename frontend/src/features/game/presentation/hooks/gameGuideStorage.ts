import type { GameGuideFlowId } from './gameGuideRegistry';

const GAME_GUIDE_STORAGE_KEY = 'game:guide:progress';

export interface GameGuideFlowState {
  readonly stepIndex: number;
  readonly completed: boolean;
  readonly promptSeen: boolean;
}

export type GameGuideStorageState = Partial<Record<GameGuideFlowId, GameGuideFlowState>>;

function getLocalStorage(): Storage | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.localStorage;
}

function isGuideFlowState(value: unknown): value is GameGuideFlowState {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  const candidate = value as Partial<GameGuideFlowState>;

  return (
    typeof candidate.stepIndex === 'number'
    && Number.isInteger(candidate.stepIndex)
    && candidate.stepIndex >= 0
    && typeof candidate.completed === 'boolean'
    && typeof candidate.promptSeen === 'boolean'
  );
}

export function createDefaultGuideFlowState(): GameGuideFlowState {
  return {
    stepIndex: 0,
    completed: false,
    promptSeen: false,
  };
}

export function getStoredFlowState(
  state: GameGuideStorageState,
  flowId: GameGuideFlowId,
): GameGuideFlowState {
  return state[flowId] ?? createDefaultGuideFlowState();
}

export function readGameGuideState(): GameGuideStorageState {
  const storage = getLocalStorage();

  if (storage === null) {
    return {};
  }

  try {
    const raw = storage.getItem(GAME_GUIDE_STORAGE_KEY);

    if (raw === null) {
      return {};
    }

    const parsed = JSON.parse(raw) as Record<string, unknown>;
    const nextState: GameGuideStorageState = {};

    for (const [key, value] of Object.entries(parsed)) {
      if ((key === 'start' || key === 'main') && isGuideFlowState(value)) {
        nextState[key] = value;
      }
    }

    return nextState;
  } catch {
    return {};
  }
}

export function writeGameGuideState(state: GameGuideStorageState): void {
  const storage = getLocalStorage();

  if (storage === null) {
    return;
  }

  storage.setItem(GAME_GUIDE_STORAGE_KEY, JSON.stringify(state));
}
