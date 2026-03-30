import React, { useState } from 'react';
import clsx from 'clsx';
import { useGameGuide } from '@features/game/presentation/hooks/useGameGuide';
import styles from './LoanConfirmModal.module.css';

interface LoanConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (requestedAmount: number) => void;
  applicationId?: number;
  contractorName: string;
  maxLoanAmount: number;
  isSubmitting?: boolean;
  error?: string | null;
}

const STEPS = ['매물 선택', '심사 완료', '신청 완료'] as const;
const CONTRACT_TEXT =
  '본 신청인은 위 기재된 서비스 이용 약관 및 대출 심사 기준에 동의하며, 제공된 정보의 허위가 없음을 보증합니다. 계약 내용에 대한 불이행 또는 오기입으로 인해 발생하는 모든 법적·경제적 책임은 본인에게 귀속됨을 확인합니다.';

// 신청 완료 = step index 2 (0-based)
const CURRENT_STEP = 2;

export const LoanConfirmModal: React.FC<LoanConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  contractorName,
  maxLoanAmount,
  isSubmitting = false,
  error = null,
}) => {
  const [rawInput, setRawInput] = useState('');
  const {
    activeFlowId,
    closeGuide,
    isOverlayVisible,
    startFlowAtStep,
  } = useGameGuide();

  if (!isOpen) return null;

  const requestedAmount = Number(rawInput.replace(/,/g, ''));
  const isValid = requestedAmount > 0 && requestedAmount <= maxLoanAmount;
  const isLoanGuideOpen = activeFlowId === 'propertyLoan' && isOverlayVisible;

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const digits = e.target.value.replace(/[^0-9]/g, '');
    setRawInput(digits ? Number(digits).toLocaleString('ko-KR') : '');
  };

  const handleConfirm = () => {
    if (!isValid) return;
    onConfirm(requestedAmount);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div
        className={styles.modal}
        onClick={(e) => e.stopPropagation()}
      >
        <div className={styles.titleRow}>
          <h2 className={styles.title}>대출 심사 이력 및 상세</h2>
          <button
            type="button"
            className={styles.guideButton}
            onClick={() => {
              if (isLoanGuideOpen) {
                closeGuide();
                return;
              }

              startFlowAtStep('propertyLoan', 'property-loan-confirm-amount');
            }}
          >
            {isLoanGuideOpen ? '가이드 닫기' : '대출 가이드'}
          </button>
        </div>

        {/* 스테퍼 */}
        <div className={styles.stepper}>
          {STEPS.map((label, idx) => (
            <React.Fragment key={label}>
              <div className={styles.stepItem}>
                <div
                  className={clsx(styles.stepCircle, {
                    [styles.stepDone]: idx < CURRENT_STEP,
                    [styles.stepCurrent]: idx === CURRENT_STEP,
                    [styles.stepPending]: idx > CURRENT_STEP,
                  })}
                >
                  {idx < CURRENT_STEP ? '✓' : ''}
                </div>
                <span className={clsx(styles.stepLabel, {
                  [styles.stepLabelActive]: idx <= CURRENT_STEP,
                })}>
                  {label}
                </span>
              </div>
              {idx < STEPS.length - 1 && (
                <div className={clsx(styles.stepLine, idx < CURRENT_STEP && styles.stepLineDone)} />
              )}
            </React.Fragment>
          ))}
        </div>

        {/* 대출 신청 금액 */}
        <h3 className={clsx(styles.sectionTitle, styles.amountTitle)}>대출 신청 금액</h3>
        <div className={styles.amountRow} data-guide="property-loan-confirm-amount">
          <span className={styles.amountPrefix}>금</span>
          <input
            className={styles.amountInput}
            type="text"
            inputMode="numeric"
            placeholder="금액을 입력해주세요"
            value={rawInput}
            onChange={handleAmountChange}
          />
          <span className={styles.amountSuffix}>원</span>
        </div>
        {rawInput && !isValid && (
          <p className={styles.errorText}>
            최대 대출 가능 금액({maxLoanAmount.toLocaleString('ko-KR')}원) 이하로 입력해주세요.
          </p>
        )}
        {error && <p className={styles.apiErrorText}>{error}</p>}

        {/* 계약서 */}
        <div className={styles.contractBox}>
          <p className={styles.contractTitle}>계약서</p>
          <p className={styles.contractText}>{CONTRACT_TEXT}</p>
          <div className={styles.contractorRow}>
            <span className={styles.contractorLabel}>계약자:</span>
            <span className={styles.contractorName}>{contractorName}</span>
            <span className={styles.contractorCheck}>✓</span>
          </div>
        </div>

        {/* 버튼 */}
        <div className={styles.buttonGroup}>
          <button
            className={styles.primaryButton}
            onClick={handleConfirm}
            disabled={!isValid || isSubmitting}
            data-guide="property-loan-confirm-action"
          >
            {isSubmitting ? '신청 중...' : '부동산 계약하러 가기'}
          </button>
          <button className={styles.secondaryButton} onClick={onClose}>
            확인 (닫기)
          </button>
        </div>
      </div>
    </div>
  );
};
