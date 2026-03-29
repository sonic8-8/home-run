import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Outlet, useLocation } from 'react-router-dom';
import { GameGuideContext, useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import { useGameGuideController } from '@features/game/presentation/hooks/useGameGuideController';
import styles from './GameGuideLayout.module.css';

function GameGuideOverlay() {
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

  return (
    <>
      <div className={styles.backdrop} aria-hidden="true" />
      <section
        className={styles.panel}
        role="dialog"
        aria-modal="false"
        aria-labelledby="game-guide-title"
      >
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
        <p className={styles.description}>{currentStep.description}</p>
        <p className={styles.hint}>{currentStep.hint}</p>
        <div className={styles.actions}>
          {activeStepIndex > 0 && (
            <button type="button" className={styles.secondaryButton} onClick={previousStep}>
              이전
            </button>
          )}
          <button type="button" className={styles.secondaryButton} onClick={closeGuide}>
            닫기
          </button>
          <button type="button" className={styles.primaryButton} onClick={nextStep}>
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

  if (isEntryPromptVisible) {
    return (
      <section className={styles.launcherCard} aria-label="게임 가이드 시작 안내">
        <p className={styles.launcherEyebrow}>처음이라면</p>
        <h2 className={styles.launcherTitle}>화면 위 가이드를 시작해보세요</h2>
        <p className={styles.launcherText}>
          현재 화면에서 눌러야 할 버튼과 다음 흐름만 짧게 안내합니다.
        </p>
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

  useEffect(() => {
    if (!isOverlayVisible || currentStep === null) {
      return;
    }

    const target = document.querySelector<HTMLElement>(`[data-guide="${currentStep.anchor}"]`);

    if (target === null) {
      return;
    }

    target.setAttribute('data-guide-active', 'true');
    if (typeof target.scrollIntoView === 'function') {
      target.scrollIntoView({
        block: 'center',
        inline: 'nearest',
      });
    }

    return () => {
      target.removeAttribute('data-guide-active');
    };
  }, [currentStep, isOverlayVisible]);

  return (
    <GameGuideContext.Provider value={controller}>
      <Outlet />
      {createPortal(
        <>
          <GameGuideOverlay />
          <GameGuideLauncher />
        </>,
        document.body,
      )}
    </GameGuideContext.Provider>
  );
}
