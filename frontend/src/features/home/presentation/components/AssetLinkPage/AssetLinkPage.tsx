import { useState } from 'react';
import { AppHeader } from '../AppHeader/AppHeader';
import type { AssetLinkInput, JobType, CardSpendCategory } from '../../../domain/entities/AssetLinkInput';
import { toErrorMessage } from '@core/error/AppError';
import { formatNumericInput, parseNumericInput } from '../../utils/money';
import styles from './AssetLinkPage.module.css';

interface AssetLinkPageProps {
  onLink: (input: AssetLinkInput) => Promise<void>;
}

const JOB_TYPE_LABELS: Record<JobType, string> = {
  SMALL_BIZ: '중소기업',
  MID_BIZ: '중견기업',
  LARGE_BIZ: '대기업',
  STARTUP: '스타트업',
  FREELANCER: '프리랜서',
};

const CARD_CATEGORY_LABELS: Record<CardSpendCategory, string> = {
  LIVING: '생활/식비',
  TRANSPORT: '교통',
  TELECOM: '통신',
  FUEL: '주유',
  MART: '마트',
  EDUCATION: '교육',
  OVERSEAS: '해외결제',
};

const ALL_CARD_CATEGORIES = Object.keys(CARD_CATEGORY_LABELS) as CardSpendCategory[];

const STEPS = ['기본 정보', '금융 자산', '카드 지출'];

export function AssetLinkPage({ onLink }: AssetLinkPageProps) {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Step 1
  const [mainBalance, setMainBalance] = useState('');
  const [salaryDay, setSalaryDay] = useState('');
  const [monthlySalary, setMonthlySalary] = useState('');
  const [monthlyFixed, setMonthlyFixed] = useState('');
  const [jobType, setJobType] = useState<JobType | ''>('');

  // Step 2
  const [depositItems, setDepositItems] = useState<{ name: string; amount: string }[]>([]);
  const [loanItems, setLoanItems] = useState<{ name: string; amount: string }[]>([]);
  const [otherIncomeItems, setOtherIncomeItems] = useState<{ name: string; amount: string }[]>([]);

  // Step 3
  const [cardSpendItems, setCardSpendItems] = useState<{ category: CardSpendCategory; amount: string }[]>([]);
  const [paymentTypes, setPaymentTypes] = useState<CardSpendCategory[]>([]);

  const validateStep1 = () => {
    if (!mainBalance) return '주 계좌 잔액을 입력해 주세요.';
    if (!salaryDay || Number(salaryDay) < 1 || Number(salaryDay) > 28) return '급여일을 1~28 사이로 입력해 주세요.';
    if (!monthlySalary) return '월 급여를 입력해 주세요.';
    if (!monthlyFixed) return '월 고정지출을 입력해 주세요.';
    if (!jobType) return '직업 유형을 선택해 주세요.';
    return null;
  };

  const validateStep3 = () => {
    if (paymentTypes.length === 0) return '주요 결제 카테고리를 1개 이상 선택해 주세요.';
    return null;
  };

  const handleNext = () => {
    if (step === 0) {
      const err = validateStep1();
      if (err) { setError(err); return; }
    }
    setError(null);
    setStep((s) => s + 1);
  };

  const handleBack = () => {
    setError(null);
    setStep((s) => s - 1);
  };

  const togglePaymentType = (cat: CardSpendCategory) => {
    setPaymentTypes((prev) => {
      if (prev.includes(cat)) return prev.filter((c) => c !== cat);
      if (prev.length >= 3) return prev;
      return [...prev, cat];
    });
  };

  const handleSubmit = async () => {
    const err = validateStep3();
    if (err) { setError(err); return; }
    setLoading(true);
    setError(null);
    try {
      const input: AssetLinkInput = {
        mainAccountBalanceAmount: parseNumericInput(mainBalance),
        salaryDayOfMonth: Number(salaryDay),
        monthlySalaryAmount: parseNumericInput(monthlySalary),
        monthlyFixedExpenseAmount: parseNumericInput(monthlyFixed),
        jobType: jobType as JobType,
        depositItems: depositItems.map((d) => ({ name: d.name, amount: parseNumericInput(d.amount) })),
        loanItems: loanItems.map((l) => ({ name: l.name, amount: parseNumericInput(l.amount) })),
        otherIncomeItems: otherIncomeItems.map((o) => ({ name: o.name, amount: parseNumericInput(o.amount) })),
        cardSpendItems: cardSpendItems.map((c) => ({ category: c.category, amount: parseNumericInput(c.amount) })),
        paymentTypes,
      };
      await onLink(input);
    } catch (error) {
      setError(toErrorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <div className={styles.card}>
          <div className={styles.stepIndicator}>
            {STEPS.map((label, i) => (
              <div key={label} className={`${styles.stepItem} ${i === step ? styles.stepActive : ''} ${i < step ? styles.stepDone : ''}`}>
                <div className={styles.stepDot}>{i < step ? '✓' : i + 1}</div>
                <span className={styles.stepLabel}>{label}</span>
              </div>
            ))}
          </div>

          {step === 0 && (
            <div className={styles.formSection}>
              <div className={styles.sectionTitle}>기본 재무 정보를 입력해 주세요</div>
              <div className={styles.field}>
                <label className={styles.label}>주 계좌 잔액 (원)</label>
                <input
                  className={styles.input}
                  type="text"
                  inputMode="numeric"
                  placeholder="예: 5000000"
                  value={mainBalance}
                  onChange={(e) => setMainBalance(formatNumericInput(e.target.value))}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>월급 입금일</label>
                <input
                  className={styles.input}
                  type="number"
                  min={1}
                  max={28}
                  placeholder="1 ~ 28"
                  value={salaryDay}
                  onChange={(e) => setSalaryDay(e.target.value)}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>월 급여 (원)</label>
                <input
                  className={styles.input}
                  type="text"
                  inputMode="numeric"
                  placeholder="예: 3000000"
                  value={monthlySalary}
                  onChange={(e) => setMonthlySalary(formatNumericInput(e.target.value))}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>월 고정지출 (원)</label>
                <input
                  className={styles.input}
                  type="text"
                  inputMode="numeric"
                  placeholder="예: 1000000"
                  value={monthlyFixed}
                  onChange={(e) => setMonthlyFixed(formatNumericInput(e.target.value))}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>직업 유형</label>
                <select
                  className={styles.select}
                  value={jobType}
                  onChange={(e) => setJobType(e.target.value as JobType)}
                >
                  <option value="">선택해 주세요</option>
                  {(Object.keys(JOB_TYPE_LABELS) as JobType[]).map((key) => (
                    <option key={key} value={key}>{JOB_TYPE_LABELS[key]}</option>
                  ))}
                </select>
              </div>
            </div>
          )}

          {step === 1 && (
            <div className={styles.formSection}>
              <div className={styles.sectionTitle}>보유 금융 자산 정보를 입력해 주세요</div>

              <div className={styles.subSection}>
                <div className={styles.subTitle}>예금 항목</div>
                {depositItems.map((item, i) => (
                  <div key={i} className={styles.itemRow}>
                    <input
                      className={styles.input}
                      type="text"
                      placeholder="이름 (예: 국민은행 적금)"
                      value={item.name}
                      onChange={(e) => {
                        const next = [...depositItems];
                        next[i] = { ...next[i], name: e.target.value };
                        setDepositItems(next);
                      }}
                    />
                    <input
                      className={styles.input}
                      type="text"
                      inputMode="numeric"
                      placeholder="잔액 (원)"
                      value={item.amount}
                      onChange={(e) => {
                        const next = [...depositItems];
                        next[i] = { ...next[i], amount: formatNumericInput(e.target.value) };
                        setDepositItems(next);
                      }}
                    />
                    <button
                      type="button"
                      className={styles.removeBtn}
                      onClick={() => setDepositItems(depositItems.filter((_, idx) => idx !== i))}
                    >삭제</button>
                  </div>
                ))}
                <button type="button" className={styles.addBtn} onClick={() => setDepositItems([...depositItems, { name: '', amount: '' }])}>
                  + 예금 추가
                </button>
              </div>

              <div className={styles.subSection}>
                <div className={styles.subTitle}>대출 항목</div>
                {loanItems.map((item, i) => (
                  <div key={i} className={styles.itemRow}>
                    <input
                      className={styles.input}
                      type="text"
                      placeholder="이름 (예: 신한 전세대출)"
                      value={item.name}
                      onChange={(e) => {
                        const next = [...loanItems];
                        next[i] = { ...next[i], name: e.target.value };
                        setLoanItems(next);
                      }}
                    />
                    <input
                      className={styles.input}
                      type="text"
                      inputMode="numeric"
                      placeholder="잔액 (원)"
                      value={item.amount}
                      onChange={(e) => {
                        const next = [...loanItems];
                        next[i] = { ...next[i], amount: formatNumericInput(e.target.value) };
                        setLoanItems(next);
                      }}
                    />
                    <button
                      type="button"
                      className={styles.removeBtn}
                      onClick={() => setLoanItems(loanItems.filter((_, idx) => idx !== i))}
                    >삭제</button>
                  </div>
                ))}
                <button type="button" className={styles.addBtn} onClick={() => setLoanItems([...loanItems, { name: '', amount: '' }])}>
                  + 대출 추가
                </button>
              </div>

              <div className={styles.subSection}>
                <div className={styles.subTitle}>기타 소득 항목</div>
                {otherIncomeItems.map((item, i) => (
                  <div key={i} className={styles.itemRow}>
                    <input
                      className={styles.input}
                      type="text"
                      placeholder="이름 (예: 유튜브 수익)"
                      value={item.name}
                      onChange={(e) => {
                        const next = [...otherIncomeItems];
                        next[i] = { ...next[i], name: e.target.value };
                        setOtherIncomeItems(next);
                      }}
                    />
                    <input
                      className={styles.input}
                      type="text"
                      inputMode="numeric"
                      placeholder="월 금액 (원)"
                      value={item.amount}
                      onChange={(e) => {
                        const next = [...otherIncomeItems];
                        next[i] = { ...next[i], amount: formatNumericInput(e.target.value) };
                        setOtherIncomeItems(next);
                      }}
                    />
                    <button
                      type="button"
                      className={styles.removeBtn}
                      onClick={() => setOtherIncomeItems(otherIncomeItems.filter((_, idx) => idx !== i))}
                    >삭제</button>
                  </div>
                ))}
                <button type="button" className={styles.addBtn} onClick={() => setOtherIncomeItems([...otherIncomeItems, { name: '', amount: '' }])}>
                  + 기타 소득 추가
                </button>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className={styles.formSection}>
              <div className={styles.sectionTitle}>카드 지출 및 결제 유형을 설정해 주세요</div>

              <div className={styles.subSection}>
                <div className={styles.subTitle}>주요 결제 카테고리 <span className={styles.required}>*</span> <span className={styles.hint}>(1~3개 선택)</span></div>
                <div className={styles.chipGroup}>
                  {ALL_CARD_CATEGORIES.map((cat) => (
                    <button
                      key={cat}
                      type="button"
                      className={paymentTypes.includes(cat) ? styles.chipActive : styles.chip}
                      onClick={() => togglePaymentType(cat)}
                    >
                      {CARD_CATEGORY_LABELS[cat]}
                    </button>
                  ))}
                </div>
              </div>

              <div className={styles.subSection}>
                <div className={styles.subTitle}>월 평균 카드 지출 <span className={styles.hint}>(선택사항, 최대 3개)</span></div>
                {cardSpendItems.map((item, i) => (
                  <div key={i} className={styles.cardItemRow}>
                    <select
                      className={styles.selectSm}
                      value={item.category}
                      onChange={(e) => {
                        const next = [...cardSpendItems];
                        next[i] = { ...next[i], category: e.target.value as CardSpendCategory };
                        setCardSpendItems(next);
                      }}
                    >
                      {ALL_CARD_CATEGORIES.filter(
                        (cat) => cat === item.category || !cardSpendItems.some((c, idx) => idx !== i && c.category === cat)
                      ).map((cat) => (
                        <option key={cat} value={cat}>{CARD_CATEGORY_LABELS[cat]}</option>
                      ))}
                    </select>
                    <input
                      className={styles.inputSm}
                      type="text"
                      inputMode="numeric"
                      placeholder="월 금액 (원)"
                      value={item.amount}
                      onChange={(e) => {
                        const next = [...cardSpendItems];
                        next[i] = { ...next[i], amount: formatNumericInput(e.target.value) };
                        setCardSpendItems(next);
                      }}
                    />
                    <button
                      type="button"
                      className={styles.removeBtn}
                      onClick={() => setCardSpendItems(cardSpendItems.filter((_, idx) => idx !== i))}
                    >삭제</button>
                  </div>
                ))}
                {cardSpendItems.length < 3 && (
                  <button
                    type="button"
                    className={styles.addBtn}
                    onClick={() => {
                      const used = new Set(cardSpendItems.map((c) => c.category));
                      const next = ALL_CARD_CATEGORIES.find((cat) => !used.has(cat));
                      if (next) setCardSpendItems([...cardSpendItems, { category: next, amount: '' }]);
                    }}
                  >
                    + 카드 지출 추가
                  </button>
                )}
              </div>
            </div>
          )}

          {error && <div className={styles.error}>{error}</div>}

          <div className={styles.btnRow}>
            {step > 0 && (
              <button type="button" className={styles.backBtn} onClick={handleBack} disabled={loading}>
                이전
              </button>
            )}
            {step < STEPS.length - 1 ? (
              <button type="button" className={styles.nextBtn} onClick={handleNext}>
                다음
              </button>
            ) : (
              <button type="button" className={styles.linkBtn} onClick={handleSubmit} disabled={loading}>
                {loading ? '연동 중...' : '마이데이터 연동하기'}
              </button>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
