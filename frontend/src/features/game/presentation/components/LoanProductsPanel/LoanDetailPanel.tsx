import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@app/routes';
import styles from './LoanDetailPanel.module.css';

interface LoanProduct {
  productId: number;
  bankName: string;
  productName: string;
  loanType: string;
  minRate: number;
  maxRate: number;
  icon: string;
}

interface Props {
  product: LoanProduct;
  sessionId: number;
  onBack: () => void;
  preSelectedPropertyId?: string;
  preSelectedPropertyName?: string;
  preSelectedPropertyPrice?: number;
  onApplyDirect?: (propertyId: string, propertyName: string, propertyPrice: number) => void;
}

const REPAYMENT_METHODS = [
  { value: 'EQUAL_PRINCIPAL_INTEREST', label: '원리금균등상환' },
  { value: 'EQUAL_PRINCIPAL',          label: '원금균등상환' },
  { value: 'BULLET',                   label: '만기일시상환' },
];

const TERM_OPTIONS = [6, 12, 24, 36, 60, 120, 180, 240, 300, 360, 600];

// 월 납입금 계산 (원리금균등상환 기준)
function calcMonthlyPayment(amount: number, annualRate: number, months: number, method: string): number {
  if (amount <= 0 || months <= 0) return 0;
  const r = annualRate / 100 / 12;
  if (method === 'BULLET') return Math.round(amount * r);
  if (method === 'EQUAL_PRINCIPAL') return Math.round(amount / months + amount * r);
  if (r === 0) return Math.round(amount / months);
  return Math.round((amount * r * Math.pow(1 + r, months)) / (Math.pow(1 + r, months) - 1));
}

export function LoanDetailPanel({ product, sessionId, onBack, preSelectedPropertyId, preSelectedPropertyName, preSelectedPropertyPrice, onApplyDirect }: Props) {
  const navigate = useNavigate();
  const [repaymentMethod, setRepaymentMethod] = useState('EQUAL_PRINCIPAL_INTEREST');
  const [termMonths, setTermMonths] = useState<number>(12);
  const [amount, setAmount] = useState('');
  const [rate, setRate] = useState(String(product.minRate));
  const [calcResult, setCalcResult] = useState<number | null>(null);

  const handleCalculate = () => {
    const parsed = Number(amount.replace(/,/g, ''));
    const r = Number(rate);
    if (!parsed || !r || !termMonths) return;
    // TODO: POST /games/sessions/{id}/loans/calculate
    setCalcResult(calcMonthlyPayment(parsed, r, termMonths, repaymentMethod));
  };

  const handleAmountChange = (v: string) => {
    const num = v.replace(/[^0-9]/g, '');
    setAmount(num ? Number(num).toLocaleString('ko-KR') : '');
    setCalcResult(null);
  };

  return (
    <div className={styles.panel}>
      {/* 헤더 */}
      <div className={styles.header}>
        <span className={styles.bankIcon}>{product.icon}</span>
        <div className={styles.headerInfo}>
          <span className={styles.productName}>{product.productName}</span>
        </div>
        <div className={styles.rateInfo}>
          <span className={styles.rateLabel}>연이율</span>
          <span className={styles.rateMain}>{product.minRate.toFixed(2)} %</span>
          <span className={styles.rateMax}>~ {product.maxRate.toFixed(2)}%</span>
        </div>
      </div>

      {/* 특징 배지 */}
      <div className={styles.features}>
        <span className={styles.badge}>원리금 균등상환</span>
        <span className={styles.badge}>최대 10억 까지</span>
        <span className={styles.badge}>최대 50년까지</span>
      </div>

      {/* 이자 계산기 */}
      <div className={styles.calculator}>
        <h3 className={styles.calcTitle}>이자 계산기</h3>
        <div className={styles.calcRow}>
          <select
            className={styles.select}
            value={repaymentMethod}
            onChange={(e) => { setRepaymentMethod(e.target.value); setCalcResult(null); }}
          >
            <option value="" disabled>상환 방식</option>
            {REPAYMENT_METHODS.map((m) => (
              <option key={m.value} value={m.value}>{m.label}</option>
            ))}
          </select>
          <select
            className={styles.select}
            value={termMonths}
            onChange={(e) => { setTermMonths(Number(e.target.value)); setCalcResult(null); }}
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
            onChange={(e) => { setRate(e.target.value); setCalcResult(null); }}
          />
        </div>
        {calcResult !== null && (
          <div className={styles.calcResult}>
            월 납입금 <strong>{calcResult.toLocaleString('ko-KR')} 원</strong>
          </div>
        )}
        <button className={styles.calcButton} onClick={handleCalculate}>
          계산 하기
        </button>
      </div>

      {/* 액션 버튼 */}
      <button
        className={styles.applyButton}
        onClick={() => {
          if (preSelectedPropertyId && preSelectedPropertyName && preSelectedPropertyPrice && onApplyDirect) {
            // 매물이 이미 선택된 경우 → GameMain에서 직접 모달 표시 (빈 화면 방지)
            onApplyDirect(preSelectedPropertyId, preSelectedPropertyName, preSelectedPropertyPrice);
          } else {
            // 일반 흐름 → 부동산 화면에서 매물 선택 후 심사
            navigate(ROUTES.PROPERTY, {
              state: { mode: 'loan-apply', productId: String(product.productId), sessionId },
            });
          }
        }}
      >
        은행 심사 넘기기
      </button>
      <button className={styles.backButton} onClick={onBack}>
        뒤로 가기
      </button>
    </div>
  );
}

export default LoanDetailPanel;
