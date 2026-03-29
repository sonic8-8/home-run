import React from 'react';
import clsx from 'clsx';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';
import styles from './LoanReviewResultModal.module.css';

interface LoanReviewResultModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGoToProperty: () => void;
  application: LoanApplication;
  propertyName?: string;
  propertyPrice?: number;
}

function formatPrice(amount: number): string {
  const uk = Math.floor(amount / 100_000_000);
  const man = Math.floor((amount % 100_000_000) / 10_000);
  if (uk > 0 && man > 0) return `${uk}억 ${man.toLocaleString('ko-KR')} 만원`;
  if (uk > 0) return `${uk}억 원`;
  return `${man.toLocaleString('ko-KR')} 만원`;
}

const STEPS = ['매물 선택', '심사 완료', '신청 완료'] as const;

export const LoanReviewResultModal: React.FC<LoanReviewResultModalProps> = ({
  isOpen,
  onClose,
  onGoToProperty,
  application,
  propertyName,
  propertyPrice,
}) => {
  if (!isOpen) return null;

  const { requestInfo, result, status } = application;
  const isApproved = status === 'APPROVED';
  const displayPropertyName = propertyName ?? '선택한 매물';
  const displayPropertyPrice = propertyPrice ?? 0;

  // 심사 완료 = step index 1 (0-based)
  const currentStep = 1;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>

        {/* 제목 */}
        <h2 className={styles.title}>대출 심사 이력 및 상세</h2>

        {/* 스테퍼 */}
        <div className={styles.stepper}>
          {STEPS.map((label, idx) => (
            <React.Fragment key={label}>
              <div className={styles.stepItem}>
                <div
                  className={clsx(styles.stepCircle, {
                    [styles.stepDone]: idx < currentStep,
                    [styles.stepCurrent]: idx === currentStep,
                    [styles.stepPending]: idx > currentStep,
                  })}
                >
                  {idx < currentStep ? '✓' : ''}
                </div>
                <span className={clsx(styles.stepLabel, {
                  [styles.stepLabelActive]: idx <= currentStep,
                })}>
                  {label}
                </span>
              </div>
              {idx < STEPS.length - 1 && (
                <div className={clsx(styles.stepLine, idx < currentStep && styles.stepLineDone)} />
              )}
            </React.Fragment>
          ))}
        </div>

        {/* 심사 요청 정보 */}
        <h3 className={styles.sectionTitle}>심사 요청 정보</h3>
        <div className={styles.infoList}>
          <div className={styles.infoRow}>
            심사요청 매물: {displayPropertyName}
          </div>
          <div className={styles.infoRow}>
            매물 가격 : {formatPrice(displayPropertyPrice)}
          </div>
          <div className={styles.infoRow}>
            신청 일시: {requestInfo.applicationDate}
          </div>
        </div>

        {/* 최종 심사 결과 */}
        <h3 className={clsx(styles.sectionTitle, styles.resultTitle)}>최종 심사 결과</h3>
        <div className={styles.resultBox}>
          {isApproved && result.maxLoanAmount != null ? (
            <>
              <span className={styles.resultLabel}>최대 대출 가능 금액</span>
              <span className={styles.resultAmount}>
                {formatPrice(result.maxLoanAmount)}
              </span>
            </>
          ) : (
            <span className={styles.rejectedText}>
              {result.rejectionReason ?? '심사가 거부되었습니다.'}
            </span>
          )}
        </div>

        {/* 버튼 */}
        <div className={styles.buttonGroup}>
          <button
            className={styles.primaryButton}
            onClick={onGoToProperty}
            disabled={!isApproved}
          >
            부동산 계약하러 가기
          </button>
          <button className={styles.secondaryButton} onClick={onClose}>
            확인 (닫기)
          </button>
        </div>
      </div>
    </div>
  );
};
