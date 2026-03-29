import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import type { LoanProduct, LoanCategory } from '@features/loan/domain/entities/LoanProduct';
import type { RepaymentMethod } from '@features/loan/domain/entities/LoanCalculation';
import { useLoan } from '@features/loan/presentation/hooks/useLoan';
import styles from './LoanDetailPanel.module.css';

interface Props {
  product: LoanProduct;
  sessionId: number;
  onBack: () => void;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
}

const REPAYMENT_METHODS = [
  { value: 'EQUAL_PRINCIPAL_INTEREST', label: '원리금균등상환' },
  { value: 'EQUAL_PRINCIPAL',          label: '원금균등상환' },
  { value: 'BULLET',                   label: '만기일시상환' },
 ] as const;

const TERM_OPTIONS = [6, 12, 24, 36, 60, 120, 180, 240, 300, 360, 600];

const DEFAULT_FEATURES: Record<Exclude<LoanCategory, 'ALL'>, string[]> = {
  CREDIT: ['신용 점수 기반 심사', '중도상환 수수료 확인 필요', '소득 증빙 필요'],
  JEONSE: ['전세 계약서 필요', '보증금 기준 심사', '임차보증금 보호 확인'],
  MORTGAGE: ['담보 가치 기반 심사', '등기부 확인 필요', '주택 가격 기준 한도 산정'],
}

function getFallbackFeatures(productType: LoanCategory): string[] {
  if (productType === 'ALL') {
    return [];
  }

  return DEFAULT_FEATURES[productType];
}

function getLoanTypeLabel(productType: LoanCategory): string {
  if (productType === 'CREDIT') {
    return '신용대출';
  }

  if (productType === 'JEONSE') {
    return '전세자금대출';
  }

  if (productType === 'MORTGAGE') {
    return '주택담보대출';
  }

  return '대출';
}

export function LoanDetailPanel({ product, sessionId, onBack, preSelectedPropertyId, preSelectedPropertyName, preSelectedPropertyPrice }: Props) {
  const navigate = useNavigate();
  const {
    selectedProduct,
    calculation,
    loading,
    error,
    fetchProductDetail,
    calculate,
  } = useLoan(sessionId);
  const [repaymentMethod, setRepaymentMethod] = useState<RepaymentMethod>('EQUAL_PRINCIPAL_INTEREST');
  const [termMonths, setTermMonths] = useState<number>(12);
  const [amount, setAmount] = useState('');
  const [rate, setRate] = useState(String(product.minRate));

  useEffect(() => {
    void fetchProductDetail(product.productId);
  }, [fetchProductDetail, product.productId]);

  const detail = selectedProduct ?? { ...product, features: [] };
  const features = detail.features.length > 0
    ? detail.features.slice(0, 3)
    : getFallbackFeatures(detail.productType);

  const handleCalculate = async () => {
    const parsed = Number(amount.replace(/,/g, ''));
    const annualRate = Number(rate);
    if (parsed <= 0 || Number.isNaN(annualRate) || annualRate < 0 || termMonths <= 0) {
      return;
    }

    await calculate({
      repaymentMethod,
      termMonths,
      principal: parsed,
      annualRate,
    });
  };

  const handleAmountChange = (v: string) => {
    const num = v.replace(/[^0-9]/g, '');
    setAmount(num ? Number(num).toLocaleString('ko-KR') : '');
  };

  return (
    <div className={styles.panel}>
      {/* 헤더 */}
      <div className={styles.header}>
        <span className={styles.bankIcon}>{detail.bankName.charAt(0)}</span>
        <div className={styles.headerInfo}>
          <span className={styles.productName}>{detail.productName}</span>
          <div className={styles.productType}>{getLoanTypeLabel(detail.productType)}</div>
        </div>
        <div className={styles.rateInfo}>
          <span className={styles.rateLabel}>연이율</span>
          <span className={styles.rateMain}>{detail.minRate.toFixed(2)} %</span>
          <span className={styles.rateMax}>~ {detail.maxRate.toFixed(2)}%</span>
        </div>
      </div>

      {/* 특징 배지 */}
      <div className={styles.features}>
        {features.map((feature) => (
          <span key={feature} className={styles.badge}>{feature}</span>
        ))}
      </div>

      {/* 이자 계산기 */}
      <div className={styles.calculator}>
        <h3 className={styles.calcTitle}>이자 계산기</h3>
        <div className={styles.calcRow}>
          <select
            className={styles.select}
            value={repaymentMethod}
            onChange={(e) => { setRepaymentMethod(e.target.value as RepaymentMethod); }}
          >
            <option value="" disabled>상환 방식</option>
            {REPAYMENT_METHODS.map((m) => (
              <option key={m.value} value={m.value}>{m.label}</option>
            ))}
          </select>
          <select
            className={styles.select}
            value={termMonths}
            onChange={(e) => { setTermMonths(Number(e.target.value)); }}
          >
            <option value="" disabled>상환 기간</option>
            {TERM_OPTIONS.map((t) => (
              <option key={t} value={t}>{t}개월</option>
            ))}
          </select>
        </div>
        <div className={styles.calcRow}>
          <input
            className={styles.input}
            placeholder="금액"
            value={amount}
            onChange={(e) => handleAmountChange(e.target.value)}
          />
          <input
            className={styles.input}
            placeholder="금리 (%)"
            value={rate}
            onChange={(e) => { setRate(e.target.value); }}
          />
        </div>
        {error && <div className={styles.statusMessage}>{error}</div>}
        {calculation !== null && (
          <div className={styles.calcResult}>
            <div>월 납입금 <strong>{calculation.monthlyPayment.toLocaleString('ko-KR')} 원</strong></div>
            <div className={styles.calcSummary}>총 이자 {calculation.totalInterest.toLocaleString('ko-KR')} 원</div>
            <div className={styles.calcSummary}>총 상환액 {calculation.totalPayment.toLocaleString('ko-KR')} 원</div>
          </div>
        )}
        <button className={styles.calcButton} type="button" onClick={() => { void handleCalculate(); }}>
          {loading ? '불러오는 중...' : '계산 하기'}
        </button>
      </div>

      {/* 액션 버튼 */}
      <button
        className={styles.applyButton}
        type="button"
        onClick={() => {
          navigate(ROUTES.REAL_ESTATE, {
            state: {
              mode: 'loan-apply',
              productId: detail.productId,
              sessionId,
              preSelectedPropertyId,
              preSelectedPropertyName,
              preSelectedPropertyPrice,
            },
          });
        }}
      >
        은행 심사 넘기기
      </button>
      <button className={styles.backButton} type="button" onClick={onBack}>
        뒤로 가기
      </button>
    </div>
  );
}

export default LoanDetailPanel;
