import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { KoreaMap } from '../../components/KoreaMap/KoreaMap';
import { LoanReviewResultModal } from '@features/loan/presentation/components/LoanReviewResultModal';
import { LoanConfirmModal } from '@features/loan/presentation/components/LoanConfirmModal';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';
import type { MapMode } from '../../constants/mapMode';

type CharacterType = 'MALE' | 'FEMALE';
type JobType =
  | 'LARGE_BIZ'
  | 'MID_BIZ'
  | 'SMALL_BIZ'
  | 'STARTUP'
  | 'FREELANCER';

interface LocationState {
  mode?: MapMode;
  sessionId?: number;
  slotNumber?: number;
  characterType?: CharacterType;
  characterName?: string;
  jobType?: JobType;
  useMyData?: boolean;
  regionCode?: string;
  districtCode?: string;
  targetPropertyId?: number;
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
  const sessionId = state.sessionId;

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

  if (mode !== 'new-game' && sessionId === undefined) {
    return <div>세션 정보를 확인하지 못했습니다.</div>;
  }

  const handlePropertySelected = (selection: {
    propertyId: string;
    propertyName: string;
    propertyPrice: number;
    regionCode: string;
    districtCode: string;
  }) => {
    if (mode === 'new-game') {
      navigate(ROUTES.GAME, {
        state: {
          ...state,
          targetPropertyId: Number(selection.propertyId),
          regionCode: selection.regionCode,
          districtCode: selection.districtCode,
        },
      });
      return;
    }

    // TODO: POST /games/sessions/{sessionId}/loans/apply { productId, propertyId }
    setLoanApplication(
      buildMockApp(selection.propertyName, selection.propertyPrice),
    );
    setReviewOpen(true);
  };

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
