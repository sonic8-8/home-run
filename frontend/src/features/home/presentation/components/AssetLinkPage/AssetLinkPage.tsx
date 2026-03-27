import { useState } from 'react';
import { AppHeader } from '../AppHeader/AppHeader';
import type { AssetLinkInput, JobType, CardSpendCategory } from '../../../domain/entities/AssetLinkInput';
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

const formatNumber = (value: string) => value.replace(/[^0-9]/g, '');

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
  const [depositItems, setDepositItems] = useState<{ balance: string }[]>([]);
  const [loanItems, setLoanItems] = useState<{ balance: string }[]>([]);
  const [otherIncomeItems, setOtherIncomeItems] = useState<{ amount: string }[]>([]);

  // Step 3
  const [cardSpendItems, setCardSpendItems] = useState<{ category: CardSpendCategory; monthlyAmount: string }[]>([]);

  const validateStep1 = () => {
    if (!mainBalance) return '주 계좌 잔액을 입력해 주세요.';
    if (!salaryDay || Number(salaryDay) < 1 || Number(salaryDay) > 31) return '급여일을 1~31 사이로 입력해 주세요.';
    if (!monthlySalary) return '월 급여를 입력해 주세요.';
    if (!monthlyFixed) return '월 고정지출을 입력해 주세요.';
    if (!jobType) return '직업 유형을 선택해 주세요.';
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

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      const input: AssetLinkInput = {
        mainAccountBalanceAmount: Number(formatNumber(mainBalance)),
        salaryDayOfMonth: Number(salaryDay),
        monthlySalaryAmount: Number(formatNumber(monthlySalary)),
        monthlyFixedExpenseAmount: Number(formatNumber(monthlyFixed)),
        jobType: jobType as JobType,
        depositItems: depositItems.map((d) => ({ balance: Number(formatNumber(d.balance)) })),
        loanItems: loanItems.map((l) => ({ balance: Number(formatNumber(l.balance)) })),
        otherIncomeItems: otherIncomeItems.map((o) => ({ amount: Number(formatNumber(o.amount)) })),
        cardSpendItems: cardSpendItems.map((c) => ({ category: c.category, monthlyAmount: Number(formatNumber(c.monthlyAmount)) })),
      };
      await onLink(input);
    } catch {
      setError('마이데이터 연동에 실패했습니다. 다시 시도해 주세요.');
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
                  onChange={(e) => setMainBalance(formatNumber(e.target.value))}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>월급 입금일</label>
                <input
                  className={styles.input}
                  type="number"
                  min={1}
                  max={31}
                  placeholder="1 ~ 31"
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
                  onChange={(e) => setMonthlySalary(formatNumber(e.target.value))}
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
                  onChange={(e) => setMonthlyFixed(formatNumber(e.target.value))}
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
                      inputMode="numeric"
                      placeholder="잔액 (원)"
                      value={item.balance}
                      onChange={(e) => {
                        const next = [...depositItems];
                        next[i] = { balance: formatNumber(e.target.value) };
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
                <button type="button" className={styles.addBtn} onClick={() => setDepositItems([...depositItems, { balance: '' }])}>
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
                      inputMode="numeric"
                      placeholder="잔액 (원)"
                      value={item.balance}
                      onChange={(e) => {
                        const next = [...loanItems];
                        next[i] = { balance: formatNumber(e.target.value) };
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
                <button type="button" className={styles.addBtn} onClick={() => setLoanItems([...loanItems, { balance: '' }])}>
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
                      inputMode="numeric"
                      placeholder="월 금액 (원)"
                      value={item.amount}
                      onChange={(e) => {
                        const next = [...otherIncomeItems];
                        next[i] = { amount: formatNumber(e.target.value) };
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
                <button type="button" className={styles.addBtn} onClick={() => setOtherIncomeItems([...otherIncomeItems, { amount: '' }])}>
                  + 기타 소득 추가
                </button>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className={styles.formSection}>
              <div className={styles.sectionTitle}>카드 지출 내역을 입력해 주세요</div>
              <div className={styles.hint}>월 평균 카드 지출 카테고리별로 입력해 주세요. (선택사항)</div>
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
                    value={item.monthlyAmount}
                    onChange={(e) => {
                      const next = [...cardSpendItems];
                      next[i] = { ...next[i], monthlyAmount: formatNumber(e.target.value) };
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
              {cardSpendItems.length < ALL_CARD_CATEGORIES.length && (
                <button
                  type="button"
                  className={styles.addBtn}
                  onClick={() => {
                    const used = new Set(cardSpendItems.map((c) => c.category));
                    const next = ALL_CARD_CATEGORIES.find((cat) => !used.has(cat));
                    if (next) setCardSpendItems([...cardSpendItems, { category: next, monthlyAmount: '' }]);
                  }}
                >
                  + 카드 지출 추가
                </button>
              )}
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
