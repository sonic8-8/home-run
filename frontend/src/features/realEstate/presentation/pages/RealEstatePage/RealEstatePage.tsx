import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import { KoreaMap } from '../../components/KoreaMap/KoreaMap';
import { LoanReviewResultModal } from '@features/loan/presentation/components/LoanReviewResultModal';
import { LoanConfirmModal } from '@features/loan/presentation/components/LoanConfirmModal';
import type { LoanApplication } from '@features/loan/domain/entities/LoanApplication';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';
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

interface SelectedProperty {
  propertyId: string;
  propertyName: string;
  propertyPrice: number;
}

export function RealEstatePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as LocationState;
  const mode = state.mode ?? 'browse';
  const sessionId = state.sessionId;
  const { apply, confirm, loading, error } = useLoan(sessionId ?? 0);
  const autoApplyRequestedRef = useRef(false);

  // loan-apply 모드이고 매물이 이미 선택된 경우 → 지도 스킵, 즉시 심사 모달
  const hasPreSelected =
    mode === 'loan-apply' &&
    !!state.preSelectedPropertyId &&
    !!state.preSelectedPropertyName &&
    !!state.preSelectedPropertyPrice;

  const [reviewOpen, setReviewOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [loanApplication, setLoanApplication] = useState<LoanApplication | null>(null);
  const [selectedProperty, setSelectedProperty] = useState<SelectedProperty | null>(
    hasPreSelected
      ? {
          propertyId: state.preSelectedPropertyId!,
          propertyName: state.preSelectedPropertyName!,
          propertyPrice: state.preSelectedPropertyPrice!,
        }
      : null,
  );

  if (mode !== 'new-game' && sessionId === undefined) {
    return <div>세션 정보를 확인하지 못했습니다.</div>;
  }

  if (mode === 'loan-apply' && state.productId === undefined) {
    return <div>대출 상품 정보를 확인하지 못했습니다.</div>;
  }

  const requestLoanReview = useCallback(async (selection: SelectedProperty) => {
    if (sessionId === undefined || state.productId === undefined) {
      return;
    }

    setSelectedProperty(selection);
    setReviewOpen(false);
    setConfirmOpen(false);

    const application = await apply(state.productId, selection.propertyId);
    if (application === null) {
      return;
    }

    setLoanApplication(application);
    setReviewOpen(true);
  }, [apply, sessionId, state.productId]);

  useEffect(() => {
    if (!hasPreSelected || autoApplyRequestedRef.current || selectedProperty === null) {
      return;
    }

    autoApplyRequestedRef.current = true;
    void requestLoanReview(selectedProperty);
  }, [hasPreSelected, requestLoanReview, selectedProperty]);

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

    void requestLoanReview({
      propertyId: selection.propertyId,
      propertyName: selection.propertyName,
      propertyPrice: selection.propertyPrice,
    });
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

  if (hasPreSelected && loanApplication === null) {
    if (loading) {
      return <div>대출 심사를 요청하는 중입니다.</div>;
    }

    if (error) {
      return (
        <div>
          <div>{error}</div>
          <button type="button" onClick={() => navigate(-1)}>뒤로 가기</button>
        </div>
      );
    }
  }

  return (
    <>
      {!confirmOpen && error && !hasPreSelected && (
        <div role="alert">{error}</div>
      )}

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
            propertyName={selectedProperty?.propertyName ?? ''}
            propertyPrice={selectedProperty?.propertyPrice ?? 0}
          />
          <LoanConfirmModal
            isOpen={confirmOpen}
            onClose={() => navigate(-1)}
            onConfirm={(amount) => {
              void (async () => {
                const confirmedLoan = await confirm(loanApplication.applicationId, amount);
                if (confirmedLoan !== null) {
                  navigate(ROUTES.GAME, { state });
                }
              })();
            }}
            contractorName="플레이어"
            maxLoanAmount={loanApplication.result.maxLoanAmount ?? 0}
            isSubmitting={loading}
            error={confirmOpen ? error : null}
          />
        </>
      )}
    </>
  );
}
