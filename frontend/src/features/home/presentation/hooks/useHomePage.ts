import type { Dashboard } from '../../domain/entities/Dashboard';
import type { SeedMoneyAccount } from '../../domain/entities/SeedMoneyAccount';
import type { CreditScore } from '../../domain/entities/CreditScore';
import type { LoanRecommendation } from '../../domain/entities/LoanRecommendation';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import type { PassSubscription } from '../../domain/entities/PassSubscription';

// TODO: 백엔드 연동 시 각 항목을 useQuery / API 호출로 교체
const MOCK_DASHBOARD: Dashboard = {
  totalAssets: 42300000,
  monthlyIncome: 2800000,
  monthlyExpense: 1420000,
  incomeChangeFromLastMonth: 0,
  expenseChangeFromLastMonth: 220000,
  nextPaydayDays: 7,
};

const MOCK_SEED_MONEY: SeedMoneyAccount = {
  bankName: '싸피은행',
  accountNumber: '110-123-000000',
  balance: 300000,
};

const MOCK_CREDIT_SCORE: CreditScore = {
  kcbScore: 936,
  niceScore: 948,
  baseScore: 936,
  estimatedMinRate: 4.89,
};

const MOCK_LOAN_RECOMMENDATIONS: LoanRecommendation[] = [
  {
    productId: 'LOAN-NH-001',
    bankName: 'NH',
    bankLogoUrl: '',
    productName: 'NH 주택담보 대출',
    productType: '주택담보대출',
    minRate: 3.49,
    maxRate: 5.89,
  },
  {
    productId: 'LOAN-KB-001',
    bankName: 'KB',
    bankLogoUrl: '',
    productName: 'KB 주택담보 대출',
    productType: '주택담보대출',
    minRate: 3.49,
    maxRate: 5.89,
  },
  {
    productId: 'LOAN-KEB-001',
    bankName: 'KEB',
    bankLogoUrl: '',
    productName: 'KEB 주택담보 대출',
    productType: '주택담보대출',
    minRate: 3.49,
    maxRate: 5.89,
  },
];

const MOCK_CARD_RECOMMENDATIONS: CardRecommendation[] = [
  { cardId: 'CARD-SS-001', cardName: 'zero 카드', cardImageUrl: '', annualFee: 0, summary: '스타벅스 리워드' },
  { cardId: 'CARD-SS-002', cardName: 'zero 카드', cardImageUrl: '', annualFee: 0, summary: '알라딘 적립' },
  { cardId: 'CARD-SS-003', cardName: 'zero 카드', cardImageUrl: '', annualFee: 0, summary: '전기요금 할인' },
  { cardId: 'CARD-SS-004', cardName: 'zero 카드', cardImageUrl: '', annualFee: 0, summary: 'AI 구독 플랫폼' },
];

const MOCK_PASS_SUBSCRIPTIONS: PassSubscription[] = [];

export const useHomePage = () => {
  return {
    dashboard: MOCK_DASHBOARD,
    seedMoney: MOCK_SEED_MONEY,
    creditScore: MOCK_CREDIT_SCORE,
    loanRecommendations: MOCK_LOAN_RECOMMENDATIONS,
    cardRecommendations: MOCK_CARD_RECOMMENDATIONS,
    passSubscriptions: MOCK_PASS_SUBSCRIPTIONS,
  };
};
