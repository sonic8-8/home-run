import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { KoreaMap } from '../../components/KoreaMap/KoreaMap';
import { LoanReviewResultModal } from '@features/loan/presentation/components/LoanReviewResultModal';
import { LoanConfirmModal } from '@features/loan/presentation/components/LoanConfirmModal';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';

export type MapMode = 'new-game' | 'loan-apply' | 'browse';

interface LocationState {
  mode?: MapMode;
  sessionId?: number;
  productId?: string;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

function buildMockApp(propertyName: string, propertyPrice: number): LoanApplication {
  return {
    applicationId: 'APP-001',
    status: 'APPROVED',
    requestInfo: {
      propertyName,
      propertyPrice,
      applicationDate: new Date().toISOString().slice(0, 10),
    },
    result: { maxLoanAmount: Math.round(propertyPrice * 0.7) },
  };
}

export function RealEstatePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const mode = state.mode ?? 'browse';
  const sessionId = state.sessionId ?? 1;

  // loan-apply 모드이고 매물이 이미 선택된 경우 → 지도 스킵, 즉시 심사 모달
  const hasPreSelected =
    mode === 'loan-apply' &&
    !!state.preSelectedPropertyId &&
    !!state.preSelectedPropertyName &&
    !!state.preSelectedPropertyPrice;

  const [reviewOpen, setReviewOpen] = useState(hasPreSelected);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [loanApplication, setLoanApplication] = useState<LoanApplication | null>(
    hasPreSelected
      ? buildMockApp(state.preSelectedPropertyName!, state.preSelectedPropertyPrice!)
      : null,
  );

  const handleLoanRequest = (propertyId: string, propertyName: string, propertyPrice: number) => {
    // browse 모드에서 대출 신청 → GameMain 대출 패널 오픈 + 매물 정보 전달
    navigate(ROUTES.GAME, {
      state: {
        ...state,
        openLoan: true,
        preSelectedPropertyId: propertyId,
        preSelectedPropertyName: propertyName,
        preSelectedPropertyPrice: propertyPrice,
      },
    });
  };

  const handlePropertySelected = (propertyId: string, propertyName: string, propertyPrice: number) => {
    if (mode === 'new-game') {
      navigate(ROUTES.GAME, {
        state: { ...state, targetPropertyId: propertyId },
      });
    } else if (mode === 'loan-apply') {
      // TODO: POST /games/sessions/{sessionId}/loans/apply { productId, propertyId }
      setLoanApplication(buildMockApp(propertyName, propertyPrice));
      setReviewOpen(true);
    }
  };

  return (
    <>
      {/* 매물이 이미 선택된 경우 지도 없이 바로 심사 모달 표시 */}
      {!hasPreSelected && (
        <KoreaMap
          sessionId={sessionId}
          mode={mode}
          onPropertySelected={handlePropertySelected}
          onLoanRequest={handleLoanRequest}
        />
      )}

      {loanApplication && (
        <>
          <LoanReviewResultModal
            isOpen={reviewOpen}
            onClose={() => navigate(-1)}
            onGoToProperty={() => {
              setReviewOpen(false);
              setConfirmOpen(true);
            }}
            application={loanApplication}
          />
          <LoanConfirmModal
            isOpen={confirmOpen}
            onClose={() => navigate(-1)}
            onConfirm={(_amount) => {
              // TODO: POST /games/sessions/{sessionId}/loans/confirm
              navigate(ROUTES.GAME, { state });
            }}
            applicationId={loanApplication.applicationId}
            contractorName="플레이어"
            maxLoanAmount={loanApplication.result.maxLoanAmount ?? 0}
          />
        </>
      )}
    </>
  );
}
