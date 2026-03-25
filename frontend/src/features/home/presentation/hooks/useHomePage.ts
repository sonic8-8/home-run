import { useState, useEffect, useCallback } from 'react';
import type { SeedMoneyAccount } from '../../domain/entities/SeedMoneyAccount';
import type { Pass } from '../../domain/entities/Pass';
import type { PassSubscription } from '../../domain/entities/PassSubscription';
import type { PassSaveResult } from '../../domain/entities/PassSaveResult';
import type { PassSubscribeResult } from '../../domain/entities/PassSubscribeResult';
import type { CardRecommendation } from '../../domain/entities/CardRecommendation';
import type { Dashboard } from '../../domain/entities/Dashboard';
import type { CreditScore } from '../../domain/entities/CreditScore';
import type { LoanRecommendationData } from '../../domain/entities/LoanRecommendation';
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

import { LoanRecommendationRemoteDataSource } from '../../data/datasources/LoanRecommendationRemoteDataSource';
import { LoanRecommendationRepositoryImpl } from '../../data/repositories/LoanRecommendationRepositoryImpl';
import { GetLoanRecommendationsUseCase } from '../../domain/usecases/GetLoanRecommendationsUseCase';

import { CreditScoreRemoteDataSource } from '../../data/datasources/CreditScoreRemoteDataSource';
import { CreditScoreRepositoryImpl } from '../../data/repositories/CreditScoreRepositoryImpl';
import { GetCreditScoreUseCase } from '../../domain/usecases/GetCreditScoreUseCase';

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

// ── Loan
const loanRepo = new LoanRecommendationRepositoryImpl(new LoanRecommendationRemoteDataSource());
const getLoanRecommendations = new GetLoanRecommendationsUseCase(loanRepo);

// ── CreditScore
const creditScoreRepo = new CreditScoreRepositoryImpl(new CreditScoreRemoteDataSource());
const getCreditScore = new GetCreditScoreUseCase(creditScoreRepo);

// TODO: Dashboard API 연동 시 교체
const MOCK_DASHBOARD: Dashboard = {
  totalAssets: 42300000,
  monthlyIncome: 2800000,
  monthlyExpense: 1420000,
  incomeChangeFromLastMonth: 0,
  expenseChangeFromLastMonth: 220000,
  nextPaydayDays: 7,
};

export const useHomePage = () => {
  const [seedMoney, setSeedMoney] = useState<SeedMoneyAccount | null>(null);
  const [allPasses, setAllPasses] = useState<Pass[]>([]);
  const [passSubscriptions, setPassSubscriptions] = useState<PassSubscription[]>([]);
  const [cardRecommendations, setCardRecommendations] = useState<CardRecommendation[]>([]);
  const [loanRecommendations, setLoanRecommendations] = useState<LoanRecommendationData | null>(null);
  const [creditScore, setCreditScore] = useState<CreditScore | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [accountResult, passesResult, subscriptionsResult, cardsResult, loansResult, creditResult] = await Promise.allSettled([
        getSeedmoneyAccount.execute(),
        getPassProducts.execute(),
        getPassSubscriptions.execute(),
        getCardRecommendations.execute(),
        getLoanRecommendations.execute(),
        getCreditScore.execute(),
      ]);
      if (accountResult.status === 'fulfilled') setSeedMoney(accountResult.value);
      if (passesResult.status === 'fulfilled') setAllPasses(passesResult.value);
      if (subscriptionsResult.status === 'fulfilled') setPassSubscriptions(subscriptionsResult.value);
      if (cardsResult.status === 'fulfilled') setCardRecommendations(cardsResult.value);
      if (loansResult.status === 'fulfilled') setLoanRecommendations(loansResult.value);
      if (creditResult.status === 'fulfilled') setCreditScore(creditResult.value);
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

  const subscribeToPas = useCallback(async (passId: number): Promise<PassSubscribeResult> => {
    const result = await subscribePass.execute(passId);
    const updated = await getPassSubscriptions.execute();
    setPassSubscriptions(updated);
    return result;
  }, []);

  const unsubscribeFromPass = useCallback(async (subscriptionId: number) => {
    await unsubscribePass.execute(subscriptionId);
    setPassSubscriptions((prev) => prev.filter((s) => s.subscriptionId !== subscriptionId));
  }, []);

  const saveToPass = useCallback(async (subscriptionId: number): Promise<PassSaveResult> => {
    const result = await savePass.execute(subscriptionId);
    setSeedMoney((prev) => prev ? { ...prev, balance: result.remainingBalance } : prev);
    return result;
  }, []);

  return {
    dashboard: MOCK_DASHBOARD,
    seedMoney,
    creditScore,
    loanRecommendations,
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
