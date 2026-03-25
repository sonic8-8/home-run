import { useState, useEffect, useCallback } from 'react';
import type { SeedMoneyAccount } from '../../domain/entities/SeedMoneyAccount';
import type { Pass } from '../../domain/entities/Pass';
import type { PassSubscription } from '../../domain/entities/PassSubscription';
import type { PassSaveResult } from '../../domain/entities/PassSaveResult';
import type { PassSubscribeResult } from '../../domain/entities/PassSubscribeResult';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import type { Dashboard } from '../../domain/entities/Dashboard';
import type { CreditScore } from '../../domain/entities/CreditScore';
import type { LoanRecommendation } from '../../domain/entities/LoanRecommendation';
import type { MyCard } from '../../domain/entities/MyCard';

import { SeedmoneyRemoteDataSource } from '../../data/datasources/SeedmoneyRemoteDataSource';
import { SeedmoneyRepositoryImpl } from '../../data/repositories/SeedmoneyRepositoryImpl';
import { GetSeedmoneyAccountUseCase } from '../../domain/usecases/GetSeedmoneyAccountUseCase';
import { TransferSeedmoneyUseCase } from '../../domain/usecases/TransferSeedmoneyUseCase';
import { DepositSeedmoneyUseCase } from '../../domain/usecases/DepositSeedmoneyUseCase';

import { PassRemoteDataSource } from '../../data/datasources/PassRemoteDataSource';
import { PassRepositoryImpl } from '../../data/repositories/PassRepositoryImpl';
import { GetPassProductsUseCase } from '../../domain/usecases/GetPassProductsUseCase';
import { GetPassSubscriptionsUseCase } from '../../domain/usecases/GetPassSubscriptionsUseCase';
import { SubscribePassUseCase } from '../../domain/usecases/SubscribePassUseCase';
import { UnsubscribePassUseCase } from '../../domain/usecases/UnsubscribePassUseCase';
import { SavePassUseCase } from '../../domain/usecases/SavePassUseCase';

import { CardRemoteDataSource } from '../../data/datasources/CardRemoteDataSource';
import { CardRepositoryImpl } from '../../data/repositories/CardRepositoryImpl';
import { GetCardRecommendationsUseCase } from '../../domain/usecases/GetCardRecommendationsUseCase';

// ── Seedmoney
const seedmoneyRepo = new SeedmoneyRepositoryImpl(new SeedmoneyRemoteDataSource());
const getSeedmoneyAccount = new GetSeedmoneyAccountUseCase(seedmoneyRepo);
const transferSeedmoney = new TransferSeedmoneyUseCase(seedmoneyRepo);
const depositSeedmoney = new DepositSeedmoneyUseCase(seedmoneyRepo);

// ── Pass
const passRepo = new PassRepositoryImpl(new PassRemoteDataSource());
const getPassProducts = new GetPassProductsUseCase(passRepo);
const getPassSubscriptions = new GetPassSubscriptionsUseCase(passRepo);
const subscribePass = new SubscribePassUseCase(passRepo);
const unsubscribePass = new UnsubscribePassUseCase(passRepo);
const savePass = new SavePassUseCase(passRepo);

// ── Card
const cardRepo = new CardRepositoryImpl(new CardRemoteDataSource());
const getCardRecommendations = new GetCardRecommendationsUseCase(cardRepo);

// TODO: Dashboard, CreditScore, LoanRecommendations API 연동 시 교체
const MOCK_DASHBOARD: Dashboard = {
  totalAssets: 42300000,
  monthlyIncome: 2800000,
  monthlyExpense: 1420000,
  incomeChangeFromLastMonth: 0,
  expenseChangeFromLastMonth: 220000,
  nextPaydayDays: 7,
};

const MOCK_CREDIT_SCORE: CreditScore = {
  kcbScore: 936,
  niceScore: 948,
  baseScore: 936,
  estimatedMinRate: 4.89,
};

const MOCK_LOAN_RECOMMENDATIONS: LoanRecommendation[] = [
  { productId: 'LOAN-NH-001', bankName: 'NH', bankLogoUrl: '', productName: 'NH 주택담보 대출', productType: '주택담보대출', minRate: 3.49, maxRate: 5.89 },
  { productId: 'LOAN-KB-001', bankName: 'KB', bankLogoUrl: '', productName: 'KB 주택담보 대출', productType: '주택담보대출', minRate: 3.49, maxRate: 5.89 },
];

export const useHomePage = () => {
  const [seedMoney, setSeedMoney] = useState<SeedMoneyAccount | null>(null);
  const [allPasses, setAllPasses] = useState<Pass[]>([]);
  const [passSubscriptions, setPassSubscriptions] = useState<PassSubscription[]>([]);
  const [cardRecommendations, setCardRecommendations] = useState<CardRecommendation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [account, passes, subscriptions, cards] = await Promise.all([
        getSeedmoneyAccount.execute(),
        getPassProducts.execute(),
        getPassSubscriptions.execute(),
        getCardRecommendations.execute(),
      ]);
      setSeedMoney(account);
      setAllPasses(passes);
      setPassSubscriptions(subscriptions);
      setCardRecommendations(cards);
    } catch (e) {
      setError(e instanceof Error ? e.message : '데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const transfer = useCallback(async (toAccountNumber: string, amount: number) => {
    const result = await transferSeedmoney.execute(toAccountNumber, amount);
    setSeedMoney((prev) => prev ? { ...prev, balance: result.remainingBalance } : prev);
    return result;
  }, []);

  const deposit = useCallback(async (fromAccountNumber: string, amount: number) => {
    const result = await depositSeedmoney.execute(fromAccountNumber, amount);
    setSeedMoney((prev) => prev ? { ...prev, balance: result.remainingBalance } : prev);
    return result;
  }, []);

  const subscribeToPas = useCallback(async (passId: number, sourceAccountId: string): Promise<PassSubscribeResult> => {
    const result = await subscribePass.execute(passId, sourceAccountId);
    const updated = await getPassSubscriptions.execute();
    setPassSubscriptions(updated);
    return result;
  }, []);

  const unsubscribeFromPass = useCallback(async (subscriptionId: number) => {
    await unsubscribePass.execute(subscriptionId);
    setPassSubscriptions((prev) => prev.filter((s) => s.subscriptionId !== subscriptionId));
  }, []);

  const saveToPass = useCallback(async (subscriptionId: number, sourceAccountId: string): Promise<PassSaveResult> => {
    const result = await savePass.execute(subscriptionId, sourceAccountId);
    setSeedMoney((prev) => prev ? { ...prev, balance: result.remainingBalance } : prev);
    return result;
  }, []);

  return {
    dashboard: MOCK_DASHBOARD,
    seedMoney,
    creditScore: MOCK_CREDIT_SCORE,
    loanRecommendations: MOCK_LOAN_RECOMMENDATIONS,
    cardRecommendations,
    passSubscriptions,
    allPasses,
    myCards: [] as MyCard[],
    loading,
    error,
    transfer,
    deposit,
    subscribeToPas,
    unsubscribeFromPass,
    saveToPass,
  };
};
