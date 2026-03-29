import React from 'react';
import styles from './JobChangeEventModal.module.css';

interface JobChangeEventModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApprove: () => void;
  onDecline: () => void;
  senderName: string;
  recipientName: string;
  date: string;
  companyName: string;
  currentSalary: number;
  offeredSalary: number;
  subject?: string;
  description?: string;
  approveLabel?: string;
  declineLabel?: string;
}

function formatSalary(amount: number): string {
  return `${(amount / 10_000).toLocaleString('ko-KR')} 만원`;
}

export const JobChangeEventModal: React.FC<JobChangeEventModalProps> = ({
  isOpen,
  onClose,
  onApprove,
  onDecline,
  senderName,
  recipientName,
  date,
  companyName,
  currentSalary,
  offeredSalary,
  subject = '열심히 일한 당신! 이직하시겠습니까?',
  description,
  approveLabel = '승인하기',
  declineLabel = '거절하기',
}) => {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* 헤더 */}
        <div className={styles.header}>
          <span className={styles.headerTitle}>새로 도착한 메일</span>
          <button className={styles.closeButton} onClick={onClose}>✕</button>
        </div>

        {/* 제목 */}
        <div className={styles.subject}>
          {subject}
        </div>

        {/* 메일 메타 정보 */}
        <div className={styles.metaSection}>
          <div className={styles.metaRow}>
            <span className={styles.metaLabel}>보낸사람</span>
            <span className={styles.chip}>{senderName}</span>
          </div>
          <div className={styles.metaRow}>
            <span className={styles.metaLabel}>받는사람</span>
            <span className={styles.chip}>{recipientName}</span>
          </div>
          <span className={styles.metaDate}>{date}</span>
        </div>

        <div className={styles.divider} />

        {/* 본문 */}
        <div className={styles.body}>
          <p>{description ?? `열심히 일한 당신에게 ${companyName}에서 이직 오퍼가 도착했습니다.`}</p>
          <p>이직시 연봉: {formatSalary(offeredSalary)}</p>
          <p>현재 연봉: {formatSalary(currentSalary)}</p>
        </div>

        {/* 버튼 */}
        <div className={styles.buttonGroup}>
          <button className={styles.approveButton} onClick={onApprove}>
            {approveLabel}
          </button>
          <button className={styles.declineButton} onClick={onDecline}>
            {declineLabel}
          </button>
        </div>
      </div>
    </div>
  );
};
