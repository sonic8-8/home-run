import { useState } from 'react';
import { MonthlyActivityModal } from '@features/game/presentation/components/MonthlyActivityModal/MonthlyActivityModal';
import { JobChangeEventModal } from '@features/game/presentation/components/JobChangeEventModal/JobChangeEventModal';
import { EventCard } from '@features/game/presentation/components/EventCard/EventCard';
import { LoanReviewResultModal } from '@features/loan/presentation/components/LoanReviewResultModal/LoanReviewResultModal';
import { LoanConfirmModal } from '@features/loan/presentation/components/LoanConfirmModal/LoanConfirmModal';
import type { TurnAction } from '@features/game/domain/entities/TurnAction';
import type { GameEvent } from '@features/game/domain/entities/GameEvent';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';
import bonusImg from '@/assets/images/event/bonus.png';

const MOCK_SHOPPING: TurnAction[] = [
  { actionType: 'GROCERY', label: '장보기', iconUrl: 'https://placehold.co/60x60?text=🛒', effects: { cash: -30000, happiness: 5 } },
  { actionType: 'CLOTHES', label: '옷 쇼핑', iconUrl: 'https://placehold.co/60x60?text=👗', effects: { cash: -80000, happiness: 10 } },
  { actionType: 'ELECTRONICS', label: '전자제품', iconUrl: 'https://placehold.co/60x60?text=💻', effects: { cash: -500000, happiness: 20, stress: -10 } },
  { actionType: 'FURNITURE', label: '가구', iconUrl: 'https://placehold.co/60x60?text=🪑', effects: { cash: -200000, happiness: 15 } },
];

const MOCK_ACTIVITIES: TurnAction[] = [
  { actionType: 'EXERCISE', label: '운동', iconUrl: 'https://placehold.co/60x60?text=🏋️', effects: { health: 20, fatigue: 10, happiness: 5 } },
  { actionType: 'STUDY', label: '공부', iconUrl: 'https://placehold.co/60x60?text=📚', effects: { knowledge: 15, fatigue: 15, stress: 10 } },
  { actionType: 'TRAVEL', label: '여행', iconUrl: 'https://placehold.co/60x60?text=✈️', effects: { cash: -300000, happiness: 30, stress: -20 } },
  { actionType: 'REST', label: '휴식', iconUrl: 'https://placehold.co/60x60?text=😴', effects: { fatigue: -30, stress: -20, happiness: 10 } },
];

const MOCK_LOAN_APPLICATION: LoanApplication = {
  applicationId: 1,
  status: 'APPROVED',
  requestInfo: {
    propertyName: '하남3지구 모아엘가 더 퍼스트',
    propertyPrice: 375_000_000,
    applicationDate: '2026.03.10',
  },
  result: {
    maxLoanAmount: 200_000_000,
  },
};

const MOCK_EVENT: GameEvent = {
  id: 'BONUS_SALARY',
  title: '보너스 지급!',
  description: '이번 달 열심히 일한 당신에게 회사에서 보너스를 지급했습니다.',
  imageSrc: bonusImg,
  cardColor: '#fffbe6',
  buttons: [
    { actionId: 'CONFIRM', label: '확인', variant: 'primary' },
    { actionId: 'VIEW_DETAILS', label: '자세히 보기', variant: 'secondary' },
  ],
};

export default function DevPreview() {
  const [showMonthly, setShowMonthly] = useState(false);
  const [showJobChange, setShowJobChange] = useState(false);
  const [showLoanReview, setShowLoanReview] = useState(false);
  const [showLoanConfirm, setShowLoanConfirm] = useState(false);

  return (
    <div style={{ padding: 32, background: '#1a1a2e', minHeight: '100vh', color: '#fff' }}>
      <h1 style={{ marginBottom: 32, fontSize: 24 }}>UI 미리보기</h1>

      {/* 버튼 그룹 */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 48 }}>
        <button
          onClick={() => setShowMonthly(true)}
          style={{ padding: '12px 24px', borderRadius: 8, background: '#4a90d9', color: '#fff', border: 'none', cursor: 'pointer', fontSize: 16 }}
        >
          월별 활동 모달 열기
        </button>
        <button
          onClick={() => setShowJobChange(true)}
          style={{ padding: '12px 24px', borderRadius: 8, background: '#e05c5c', color: '#fff', border: 'none', cursor: 'pointer', fontSize: 16 }}
        >
          이직 이벤트 모달 열기
        </button>
        <button
          onClick={() => setShowLoanReview(true)}
          style={{ padding: '12px 24px', borderRadius: 8, background: '#22c55e', color: '#fff', border: 'none', cursor: 'pointer', fontSize: 16 }}
        >
          대출 심사 결과 모달 열기
        </button>
      </div>

      {/* 이벤트 카드 인라인 표시 */}
      <h2 style={{ marginBottom: 16, fontSize: 18 }}>이벤트 카드</h2>
      <div style={{ display: 'inline-block' }}>
        <EventCard
          event={MOCK_EVENT}
          onAction={(id) => alert(`액션: ${id}`)}
        />
      </div>

      {/* 모달들 */}
      <MonthlyActivityModal
        isOpen={showMonthly}
        month={3}
        shopping={MOCK_SHOPPING}
        activities={MOCK_ACTIVITIES}
        preview={null}
        commitResult={null}
        isActionsLoading={false}
        isSubmitting={false}
        isCommitting={false}
        error={null}
        onClose={() => setShowMonthly(false)}
        onSubmitSlots={(slots) => { alert(`선택: ${slots.join(', ')}`); }}
        onBackToSelection={() => {}}
        onCommit={() => { alert('턴 진행 확정'); setShowMonthly(false); }}
        onConfirmCommitResult={() => setShowMonthly(false)}
      />

      <LoanReviewResultModal
        isOpen={showLoanReview}
        onClose={() => setShowLoanReview(false)}
        onGoToProperty={() => { setShowLoanReview(false); setShowLoanConfirm(true); }}
        application={MOCK_LOAN_APPLICATION}
      />

      <LoanConfirmModal
        isOpen={showLoanConfirm}
        onClose={() => setShowLoanConfirm(false)}
        onConfirm={(amount) => { alert(`대출 신청 완료: ${amount.toLocaleString('ko-KR')}원`); setShowLoanConfirm(false); }}
        applicationId={MOCK_LOAN_APPLICATION.applicationId}
        contractorName="김싸피"
        maxLoanAmount={MOCK_LOAN_APPLICATION.result.maxLoanAmount ?? 0}
      />

      <JobChangeEventModal
        isOpen={showJobChange}
        onClose={() => setShowJobChange(false)}
        onApprove={() => { alert('이직 승인'); setShowJobChange(false); }}
        onDecline={() => { alert('이직 거절'); setShowJobChange(false); }}
        senderName="삼성전자 HR팀"
        recipientName="김개발"
        date="2026년 3월 18일"
        companyName="삼성전자"
        currentSalary={40_000_000}
        offeredSalary={55_000_000}
      />
    </div>
  );
}
