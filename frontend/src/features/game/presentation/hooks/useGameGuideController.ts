import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  GAME_GUIDE_FLOWS,
  getGuideFlow,
  getGuideFlowByPath,
  getGuideSteps,
  matchesGuideStepPath,
  type GameGuideFlowId,
  type GameGuideStep,
} from './gameGuideRegistry';
import {
  getStoredFlowState,
  readGameGuideState,
  writeGameGuideState,
  type GameGuideStorageState,
} from './gameGuideStorage';

export interface GameGuideControllerValue {
  readonly activeFlowId: GameGuideFlowId | null;
  readonly activeStepIndex: number;
  readonly currentStep: GameGuideStep | null;
  readonly isOverlayVisible: boolean;
  readonly isEntryPromptVisible: boolean;
  readonly launcherFlowId: GameGuideFlowId | null;
  readonly isLauncherVisible: boolean;
  readonly isLastStep: boolean;
  readonly stepCount: number;
  readonly toggleFlow: (flowId: GameGuideFlowId) => void;
  readonly startOrResumeFlow: (flowId: GameGuideFlowId) => void;
  readonly startFlowAtStep: (flowId: GameGuideFlowId, stepId: string) => void;
  readonly dismissPrompt: (flowId: GameGuideFlowId) => void;
  readonly closeGuide: () => void;
  readonly nextStep: () => void;
  readonly previousStep: () => void;
  readonly getTriggerLabel: (flowId: GameGuideFlowId) => string;
}

export function useGameGuideController(pathname: string): GameGuideControllerValue {
  const [flowStates, setFlowStates] = useState<GameGuideStorageState>(() => readGameGuideState());
  const [activeFlowId, setActiveFlowId] = useState<GameGuideFlowId | null>(null);
  const [activeStepIndex, setActiveStepIndex] = useState(0);

  const updateFlowState = useCallback(
    (
      flowId: GameGuideFlowId,
      updater: (current: ReturnType<typeof getStoredFlowState>) => ReturnType<typeof getStoredFlowState>,
    ) => {
      setFlowStates((currentState) => {
        const nextState: GameGuideStorageState = {
          ...currentState,
          [flowId]: updater(getStoredFlowState(currentState, flowId)),
        };
        writeGameGuideState(nextState);
        return nextState;
      });
    },
    [],
  );

  const activeSteps = useMemo(
    () => (activeFlowId === null ? [] : getGuideSteps(activeFlowId)),
    [activeFlowId],
  );
  const currentStep = activeFlowId === null ? null : activeSteps[activeStepIndex] ?? null;
  const currentFlow = activeFlowId === null ? null : getGuideFlow(activeFlowId);

  useEffect(() => {
    if (activeFlowId === null || currentFlow === null || currentStep === null) {
      return;
    }

    if (matchesGuideStepPath(currentStep, pathname)) {
      return;
    }

    const nextMatchingStepIndex = currentFlow.steps.findIndex(
      (step, index) => index >= activeStepIndex && matchesGuideStepPath(step, pathname),
    );

    if (nextMatchingStepIndex !== -1) {
      setActiveStepIndex(nextMatchingStepIndex);
      updateFlowState(activeFlowId, (state) => ({
        ...state,
        stepIndex: nextMatchingStepIndex,
        completed: false,
        promptSeen: true,
      }));
      return;
    }

    const firstMatchingStepIndex = currentFlow.steps.findIndex((step) =>
      matchesGuideStepPath(step, pathname),
    );

    if (firstMatchingStepIndex === -1) {
      setActiveFlowId(null);
      return;
    }

    setActiveStepIndex(firstMatchingStepIndex);
    updateFlowState(activeFlowId, (state) => ({
      ...state,
      stepIndex: firstMatchingStepIndex,
      completed: false,
      promptSeen: true,
    }));
  }, [pathname, activeFlowId, activeStepIndex, currentFlow, currentStep, updateFlowState]);

  const matchedFlowId = useMemo(() => getGuideFlowByPath(pathname), [pathname]);
  const launcherFlowId = activeFlowId === null ? matchedFlowId : null;
  const launcherFlowState = launcherFlowId === null ? null : getStoredFlowState(flowStates, launcherFlowId);
  const isEntryPromptVisible = (
    launcherFlowId !== null
    && launcherFlowState !== null
    && !launcherFlowState.promptSeen
    && !launcherFlowState.completed
    && GAME_GUIDE_FLOWS[launcherFlowId].entryPath === pathname
  );
  const isLauncherVisible = (
    launcherFlowId !== null
    && launcherFlowState !== null
    && !isEntryPromptVisible
    && (
      launcherFlowState.stepIndex > 0
      || (
        launcherFlowState.completed
        && GAME_GUIDE_FLOWS[launcherFlowId].entryPath === pathname
      )
    )
  );
  const isOverlayVisible = currentStep !== null && matchesGuideStepPath(currentStep, pathname);
  const isLastStep = currentStep !== null && activeStepIndex === activeSteps.length - 1;

  const startOrResumeFlow = useCallback(
    (flowId: GameGuideFlowId) => {
      const storedState = getStoredFlowState(flowStates, flowId);
      const nextStepIndex = storedState.completed
        ? 0
        : Math.min(storedState.stepIndex, getGuideSteps(flowId).length - 1);

      setActiveFlowId(flowId);
      setActiveStepIndex(nextStepIndex);
      updateFlowState(flowId, (state) => ({
        ...state,
        stepIndex: nextStepIndex,
        completed: false,
        promptSeen: true,
      }));
    },
    [flowStates, updateFlowState],
  );

  const startFlowAtStep = useCallback(
    (flowId: GameGuideFlowId, stepId: string) => {
      const steps = getGuideSteps(flowId);
      const requestedStepIndex = steps.findIndex((step) => step.id === stepId);
      const nextStepIndex = requestedStepIndex === -1 ? 0 : requestedStepIndex;

      setActiveFlowId(flowId);
      setActiveStepIndex(nextStepIndex);
      updateFlowState(flowId, (state) => ({
        ...state,
        stepIndex: nextStepIndex,
        completed: false,
        promptSeen: true,
      }));
    },
    [updateFlowState],
  );

  const dismissPrompt = useCallback(
    (flowId: GameGuideFlowId) => {
      updateFlowState(flowId, (state) => ({
        ...state,
        promptSeen: true,
      }));
    },
    [updateFlowState],
  );

  const closeGuide = useCallback(() => {
    if (activeFlowId === null) {
      return;
    }

    updateFlowState(activeFlowId, (state) => ({
      ...state,
      stepIndex: activeStepIndex,
      completed: false,
      promptSeen: true,
    }));
    setActiveFlowId(null);
  }, [activeFlowId, activeStepIndex, updateFlowState]);

  const nextStep = useCallback(() => {
    if (activeFlowId === null) {
      return;
    }

    const nextStepIndex = activeStepIndex + 1;
    const steps = getGuideSteps(activeFlowId);

    if (nextStepIndex >= steps.length) {
      updateFlowState(activeFlowId, (state) => ({
        ...state,
        stepIndex: 0,
        completed: true,
        promptSeen: true,
      }));
      setActiveFlowId(null);
      setActiveStepIndex(0);
      return;
    }

    updateFlowState(activeFlowId, (state) => ({
      ...state,
      stepIndex: nextStepIndex,
      completed: false,
      promptSeen: true,
    }));
    setActiveStepIndex(nextStepIndex);
  }, [activeFlowId, activeStepIndex, updateFlowState]);

  const previousStep = useCallback(() => {
    if (activeFlowId === null || activeStepIndex === 0) {
      return;
    }

    const previousStepIndex = activeStepIndex - 1;
    updateFlowState(activeFlowId, (state) => ({
      ...state,
      stepIndex: previousStepIndex,
      completed: false,
      promptSeen: true,
    }));
    setActiveStepIndex(previousStepIndex);
  }, [activeFlowId, activeStepIndex, updateFlowState]);

  const toggleFlow = useCallback(
    (flowId: GameGuideFlowId) => {
      if (activeFlowId === flowId && isOverlayVisible) {
        closeGuide();
        return;
      }

      startOrResumeFlow(flowId);
    },
    [activeFlowId, closeGuide, isOverlayVisible, startOrResumeFlow],
  );

  const getTriggerLabel = useCallback(
    (flowId: GameGuideFlowId) => {
      if (activeFlowId === flowId && isOverlayVisible) {
        return '가이드 닫기';
      }

      const state = getStoredFlowState(flowStates, flowId);

      if (state.completed) {
        return '가이드 다시 보기';
      }

      if (state.stepIndex > 0) {
        return '가이드 이어보기';
      }

      return '게임 가이드';
    },
    [activeFlowId, flowStates, isOverlayVisible],
  );

  return {
    activeFlowId,
    activeStepIndex,
    currentStep,
    isOverlayVisible,
    isEntryPromptVisible,
    launcherFlowId,
    isLauncherVisible,
    isLastStep,
    stepCount: activeSteps.length,
    toggleFlow,
    startOrResumeFlow,
    startFlowAtStep,
    dismissPrompt,
    closeGuide,
    nextStep,
    previousStep,
    getTriggerLabel,
  };
}
