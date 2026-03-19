import type { Property, PropertySummary } from '../../../domain/entities/Property';
import type { MapMode } from '../../pages/RealEstatePage/RealEstatePage';
import { formatPriceWon, toSquarePyeong } from '../../utils/formatUtils';

const HOUSING_TYPE_LABEL: Record<string, string> = {
  NONE: '무주거',
  STUDIO: '원룸/고시원',
  VILLA: '빌라/오피스텔',
  JEONSE_APT: '아파트 전세',
  OWNED_APT: '아파트 자가',
};

export function PropertyDetailPanel({
  summary,
  detail,
  onClose,
  mode = 'browse',
  onSelect,
  onLoanRequest,
}: {
  summary: PropertySummary;
  detail: Property | null;
  onClose: () => void;
  mode?: MapMode;
  onSelect?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
  onLoanRequest?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}) {
  const pyeong = detail ? toSquarePyeong(detail.specs.area) : null;

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: 360,
        height: '100%',
        background: '#fff',
        zIndex: 30,
        fontFamily: "'Pretendard', 'Apple SD Gothic Neo', sans-serif",
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '4px 0 24px rgba(0,0,0,0.12)',
        overflow: 'hidden',
      }}
    >
      {/* 헤더 */}
      <div
        style={{
          padding: '20px 16px 16px',
          background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
          color: '#fff',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div style={{ flex: 1, marginRight: 12 }}>
            <div style={{ fontSize: 16, fontWeight: 800, letterSpacing: '-0.02em', lineHeight: 1.3 }}>
              {summary.name}
            </div>
            {detail && (
              <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.8)', marginTop: 4 }}>
                {HOUSING_TYPE_LABEL[detail.housingType] ?? detail.housingType}
              </div>
            )}
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'rgba(255,255,255,0.2)',
              border: 'none',
              borderRadius: '50%',
              width: 28,
              height: 28,
              fontSize: 14,
              cursor: 'pointer',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            ✕
          </button>
        </div>
      </div>

      {/* 가격 섹션 */}
      <div style={{ padding: '20px 16px', borderBottom: '1px solid #f3f4f6' }}>
        <div style={{ fontSize: 11, color: '#9ca3af', marginBottom: 4 }}>현재 시세</div>
        <div style={{ fontSize: 32, fontWeight: 800, color: '#6366f1', letterSpacing: '-0.03em' }}>
          {formatPriceWon(summary.recentPrice)}
        </div>
        {detail?.deposit != null && detail.deposit > 0 && (
          <div style={{ fontSize: 12, color: '#6b7280', marginTop: 4 }}>
            보증금 {formatPriceWon(detail.deposit)}
          </div>
        )}
      </div>

      {/* 상세 정보 */}
      {detail ? (
        <div style={{ padding: '16px', borderBottom: '1px solid #f3f4f6', flex: 1, overflowY: 'auto' }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: '#374151', marginBottom: 12 }}>
            매물 정보
          </div>
          <InfoRow label="주소" value={detail.address} />
          {pyeong != null && (
            <InfoRow label="면적" value={`${pyeong}평 (${detail.specs.area.toFixed(1)}㎡)`} />
          )}
          <InfoRow label="층수" value={detail.specs.floor} />
          <InfoRow label="방향" value={detail.specs.direction} />
          {detail.maintenanceFee > 0 && (
            <InfoRow label="관리비" value={`월 ${(detail.maintenanceFee / 10000).toFixed(0)}만원`} />
          )}
        </div>
      ) : (
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ fontSize: 13, color: '#9ca3af' }}>상세 정보 불러오는 중...</div>
        </div>
      )}

      {/* 액션 버튼 */}
      <div style={{ padding: '16px', display: 'flex', gap: 8 }}>
        {mode === 'new-game' || mode === 'loan-apply' ? (
          <button
            style={{
              flex: 1,
              padding: '12px',
              background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              border: 'none',
              borderRadius: 10,
              fontSize: 13,
              fontWeight: 700,
              color: '#fff',
              cursor: 'pointer',
              fontFamily: 'inherit',
            }}
            onClick={() => onSelect?.(summary.propertyId, summary.name, summary.recentPrice)}
          >
            {mode === 'new-game' ? '이 매물로 시작하기' : '이 매물로 대출 신청'}
          </button>
        ) : (
          <>
            <button
              style={{
                flex: 1,
                padding: '12px',
                background: '#f9fafb',
                border: '1px solid #e5e7eb',
                borderRadius: 10,
                fontSize: 13,
                fontWeight: 700,
                color: '#6b7280',
                cursor: 'pointer',
                fontFamily: 'inherit',
              }}
              onClick={() => onLoanRequest?.(summary.propertyId, summary.name, summary.recentPrice)}
            >
              대출 신청
            </button>
            <button
              style={{
                flex: 1,
                padding: '12px',
                background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                border: 'none',
                borderRadius: 10,
                fontSize: 13,
                fontWeight: 700,
                color: '#fff',
                cursor: 'pointer',
                fontFamily: 'inherit',
              }}
              onClick={() => {
                // TODO: open purchase/contract flow
              }}
            >
              구매하기
            </button>
          </>
        )}
      </div>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '7px 0',
        borderBottom: '1px solid #f9fafb',
      }}
    >
      <span style={{ fontSize: 12, color: '#9ca3af', minWidth: 48 }}>{label}</span>
      <span style={{ fontSize: 13, fontWeight: 600, color: '#374151', textAlign: 'right' }}>{value}</span>
    </div>
  );
}
