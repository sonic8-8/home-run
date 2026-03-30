import { useEffect, useRef, useState, type CSSProperties } from 'react';
import { createPortal } from 'react-dom';
import { Outlet, useLocation } from 'react-router-dom';
import {
  getGuideFlow,
  type GameGuideStep,
} from '@features/game/presentation/hooks/gameGuideRegistry';
import { GameGuideContext, useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import { useGameGuideController } from '@features/game/presentation/hooks/useGameGuideController';
import styles from './GameGuideLayout.module.css';

type GuidePanelPlacement = 'top' | 'bottom';

interface GuideTargetState {
  readonly isReady: boolean;
  readonly placement: GuidePanelPlacement;
  readonly panelStyle?: CSSProperties;
  readonly pointerStyle?: CSSProperties;
}

const MOBILE_BREAKPOINT = 640;
const DESKTOP_PANEL_WIDTH = 420;
const DESKTOP_EDGE_GAP = 24;
const TARGET_PANEL_GAP = 18;
const ESTIMATED_PANEL_HEIGHT = 320;
const DEFAULT_GUIDE_TARGET_STATE: GuideTargetState = {
  isReady: false,
  placement: 'bottom',
};

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

function scheduleDomRead(callback: () => void): number {
  if (typeof window.requestAnimationFrame === 'function') {
    return window.requestAnimationFrame(callback);
  }

  return window.setTimeout(callback, 0);
}

function cancelScheduledDomRead(handle: number): void {
  if (typeof window.cancelAnimationFrame === 'function') {
    window.cancelAnimationFrame(handle);
    return;
  }

  window.clearTimeout(handle);
}

function buildGuideTargetState(target: HTMLElement): GuideTargetState {
  const rect = target.getBoundingClientRect();
  const computedStyle = window.getComputedStyle(target);

  if (
    computedStyle.display === 'none'
    || computedStyle.visibility === 'hidden'
    || computedStyle.opacity === '0'
  ) {
    return DEFAULT_GUIDE_TARGET_STATE;
  }

  if (window.innerWidth <= MOBILE_BREAKPOINT || (rect.width === 0 && rect.height === 0)) {
    return {
      isReady: true,
      placement: 'bottom',
    };
  }

  const roomAbove = rect.top;
  const roomBelow = window.innerHeight - rect.bottom;
  const canPlaceAbove = roomAbove >= ESTIMATED_PANEL_HEIGHT + TARGET_PANEL_GAP;
  const canPlaceBelow = roomBelow >= ESTIMATED_PANEL_HEIGHT + TARGET_PANEL_GAP;
  const placement: GuidePanelPlacement = canPlaceBelow || (!canPlaceAbove && roomBelow >= roomAbove)
    ? 'bottom'
    : 'top';
  const panelWidth = Math.min(DESKTOP_PANEL_WIDTH, window.innerWidth - DESKTOP_EDGE_GAP * 2);
  const left = clamp(
    rect.left + rect.width / 2 - panelWidth / 2,
    DESKTOP_EDGE_GAP,
    window.innerWidth - panelWidth - DESKTOP_EDGE_GAP,
  );
  const pointerCenter = clamp(rect.left + rect.width / 2 - left, 28, panelWidth - 28);
  const top = placement === 'bottom'
    ? clamp(
        rect.bottom + TARGET_PANEL_GAP,
        DESKTOP_EDGE_GAP,
        window.innerHeight - ESTIMATED_PANEL_HEIGHT - DESKTOP_EDGE_GAP,
      )
    : Math.max(DESKTOP_EDGE_GAP + ESTIMATED_PANEL_HEIGHT, rect.top - TARGET_PANEL_GAP);

  return {
    isReady: true,
    placement,
    panelStyle: {
      top: `${Math.round(top)}px`,
      left: `${Math.round(left)}px`,
      right: 'auto',
      bottom: 'auto',
    },
    pointerStyle: {
      left: `${Math.round(pointerCenter)}px`,
    },
  };
}

function getMissingGuideCopy(step: GameGuideStep): string {
  return step.missingDescription
    ?? `${step.targetLabel}이 보이면 강조가 붙고 다음 단계가 열립니다.`;
}

function GameGuideOverlay({ guideTarget }: { guideTarget: GuideTargetState }) {
  const {
    activeStepIndex,
    closeGuide,
    currentStep,
    isLastStep,
    isOverlayVisible,
    nextStep,
    previousStep,
    stepCount,
  } = useGameGuide();

  if (!isOverlayVisible || currentStep === null) {
    return null;
  }

  const currentFlow = getGuideFlow(currentStep.flowId);
  const isNextDisabled = !guideTarget.isReady && !currentStep.allowAdvanceWithoutTarget;

  return (
    <>
      <div className={styles.backdrop} aria-hidden="true" />
      <section
        className={`${styles.panel} ${guideTarget.isReady && guideTarget.panelStyle !== undefined ? styles.panelFloating : ''}`}
        data-placement={guideTarget.placement}
        role="dialog"
        aria-modal="false"
        aria-labelledby="game-guide-title"
        aria-describedby="game-guide-description"
        style={guideTarget.panelStyle}
      >
        {guideTarget.isReady && guideTarget.pointerStyle !== undefined && (
          <span className={styles.pointer} style={guideTarget.pointerStyle} aria-hidden="true" />
        )}
        <div className={styles.header}>
          <div>
            <p className={styles.eyebrow}>GAME GUIDE</p>
            <h2 id="game-guide-title" className={styles.title}>
              {currentStep.title}
            </h2>
          </div>
          <span className={styles.progress}>
            {activeStepIndex + 1} / {stepCount}
          </span>
        </div>
        <div className={styles.metaRow}>
          <span className={styles.flowBadge}>{currentFlow.label}</span>
          <span className={guideTarget.isReady ? styles.targetBadgeReady : styles.targetBadgePending}>
            대상: {currentStep.targetLabel}
          </span>
        </div>
        {!guideTarget.isReady && (
          <div className={styles.statusCard} role="status">
            <strong className={styles.statusTitle}>
              {currentStep.targetLabel}이 아직 화면에 열려 있지 않습니다.
            </strong>
            <p className={styles.statusText}>{getMissingGuideCopy(currentStep)}</p>
          </div>
        )}
        <p id="game-guide-description" className={styles.description}>
          {currentStep.description}
        </p>
        <p className={styles.hint}>
          {guideTarget.isReady
            ? currentStep.hint
            : currentStep.allowAdvanceWithoutTarget
              ? '이 단계는 상황에 따라 생략될 수 있어, 원하면 다음으로 넘어갈 수 있습니다.'
              : '대상이 준비되면 다음 버튼이 활성화됩니다.'}
        </p>
        <div className={styles.actions}>
          {activeStepIndex > 0 && (
            <button type="button" className={styles.secondaryButton} onClick={previousStep}>
              이전
            </button>
          )}
          <button type="button" className={styles.secondaryButton} onClick={closeGuide}>
            닫기
          </button>
          <button
            type="button"
            className={styles.primaryButton}
            onClick={nextStep}
            disabled={isNextDisabled}
          >
            {isLastStep ? '가이드 마치기' : '다음'}
          </button>
        </div>
      </section>
    </>
  );
}

function GameGuideLauncher() {
  const {
    dismissPrompt,
    getTriggerLabel,
    isEntryPromptVisible,
    isLauncherVisible,
    launcherFlowId,
    startOrResumeFlow,
  } = useGameGuide();

  if (launcherFlowId === null) {
    return null;
  }

  const launcherFlow = getGuideFlow(launcherFlowId);

  if (isEntryPromptVisible) {
    return (
      <section className={styles.launcherCard} aria-label="게임 가이드 시작 안내">
        <p className={styles.launcherEyebrow}>{launcherFlow.launcherEyebrow}</p>
        <h2 className={styles.launcherTitle}>{launcherFlow.launcherTitle}</h2>
        <p className={styles.launcherText}>{launcherFlow.launcherDescription}</p>
        <div className={styles.launcherActions}>
          <button
            type="button"
            className={styles.secondaryButton}
            onClick={() => dismissPrompt(launcherFlowId)}
          >
            나중에
          </button>
          <button
            type="button"
            className={styles.primaryButton}
            onClick={() => startOrResumeFlow(launcherFlowId)}
          >
            가이드 시작
          </button>
        </div>
      </section>
    );
  }

  if (!isLauncherVisible) {
    return null;
  }

  return (
    <button
      type="button"
      className={styles.resumeButton}
      onClick={() => startOrResumeFlow(launcherFlowId)}
    >
      {getTriggerLabel(launcherFlowId)}
    </button>
  );
}

export function GameGuideLayout() {
  const location = useLocation();
  const controller = useGameGuideController(location.pathname);
  const { currentStep, isOverlayVisible } = controller;
  const [guideTarget, setGuideTarget] = useState<GuideTargetState>(DEFAULT_GUIDE_TARGET_STATE);
  const activeTargetRef = useRef<HTMLElement | null>(null);
  const scrolledStepKeyRef = useRef<string | null>(null);

  useEffect(() => {
    const clearActiveTarget = () => {
      if (activeTargetRef.current !== null) {
        activeTargetRef.current.removeAttribute('data-guide-active');
        activeTargetRef.current = null;
      }
    };

    if (!isOverlayVisible || currentStep === null) {
      clearActiveTarget();
      scrolledStepKeyRef.current = null;
      setGuideTarget(DEFAULT_GUIDE_TARGET_STATE);
      return;
    }

    const selector = `[data-guide="${currentStep.anchor}"]`;
    const stepKey = `${currentStep.flowId}:${currentStep.id}`;
    let scheduledReadHandle = 0;

    const syncTarget = () => {
      const nextTarget = document.querySelector<HTMLElement>(selector);

      if (activeTargetRef.current !== nextTarget) {
        clearActiveTarget();

        if (nextTarget !== null) {
          nextTarget.setAttribute('data-guide-active', 'true');
        }

        activeTargetRef.current = nextTarget;
      }

      if (nextTarget === null) {
        setGuideTarget((previousState) => (
          previousState.isReady ? DEFAULT_GUIDE_TARGET_STATE : previousState
        ));
        return;
      }

      if (scrolledStepKeyRef.current !== stepKey && typeof nextTarget.scrollIntoView === 'function') {
        nextTarget.scrollIntoView({
          block: 'center',
          inline: 'nearest',
        });
        scrolledStepKeyRef.current = stepKey;
      }

      const nextGuideTarget = buildGuideTargetState(nextTarget);
      setGuideTarget((previousState) => {
        if (
          previousState.isReady === nextGuideTarget.isReady
          && previousState.placement === nextGuideTarget.placement
          && previousState.panelStyle?.top === nextGuideTarget.panelStyle?.top
          && previousState.panelStyle?.left === nextGuideTarget.panelStyle?.left
          && previousState.pointerStyle?.left === nextGuideTarget.pointerStyle?.left
        ) {
          return previousState;
        }

        return nextGuideTarget;
      });
    };

    const queueSyncTarget = () => {
      cancelScheduledDomRead(scheduledReadHandle);
      scheduledReadHandle = scheduleDomRead(syncTarget);
    };

    queueSyncTarget();

    const observer = new MutationObserver(queueSyncTarget);
    observer.observe(document.body, {
      attributes: true,
      childList: true,
      subtree: true,
    });

    window.addEventListener('resize', queueSyncTarget);
    window.addEventListener('scroll', queueSyncTarget, true);

    return () => {
      cancelScheduledDomRead(scheduledReadHandle);
      observer.disconnect();
      window.removeEventListener('resize', queueSyncTarget);
      window.removeEventListener('scroll', queueSyncTarget, true);
      clearActiveTarget();
    };
  }, [currentStep, isOverlayVisible]);

  return (
    <GameGuideContext.Provider value={controller}>
      <Outlet />
      {createPortal(
        <>
          <GameGuideOverlay guideTarget={guideTarget} />
          <GameGuideLauncher />
        </>,
        document.body,
      )}
    </GameGuideContext.Provider>
  );
}
