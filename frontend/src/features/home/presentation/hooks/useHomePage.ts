import { useState, useEffect, useCallback } from 'react';
import { useDashboardSse } from './useDashboardSse';
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
import type { Spending } from '../../domain/entities/Spending';

import { UserRemoteDataSource } from '../../data/datasources/UserRemoteDataSource';
import { UserRepositoryImpl } from '../../data/repositories/UserRepositoryImpl';
import { GetUserMeUseCase } from '../../domain/usecases/GetUserMeUseCase';
import { LinkAssetsUseCase } from '../../domain/usecases/LinkAssetsUseCase';

import { DashboardRemoteDataSource } from '../../data/datasources/DashboardRemoteDataSource';
import { DashboardRepositoryImpl } from '../../data/repositories/DashboardRepositoryImpl';
import { GetDashboardUseCase } from '../../domain/usecases/GetDashboardUseCase';

import { SpendingRemoteDataSource } from '../../data/datasources/SpendingRemoteDataSource';
import { SpendingRepositoryImpl } from '../../data/repositories/SpendingRepositoryImpl';
import { GetSpendingUseCase } from '../../domain/usecases/GetSpendingUseCase';

import { SeedmoneyRemoteDataSource } from '../../data/datasources/SeedmoneyRemoteDataSource';
import { SeedmoneyRepositoryImpl } from '../../data/repositories/SeedmoneyRepositoryImpl';
import { CreateSeedmoneyAccountUseCase } from '../../domain/usecases/CreateSeedmoneyAccountUseCase';
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
import { GetPassHistoryUseCase } from '../../domain/usecases/GetPassHistoryUseCase';
import type { PassHistoryPage } from '../../domain/entities/PassHistory';

import { CardRemoteDataSource } from '../../data/datasources/CardRemoteDataSource';
import { CardRepositoryImpl } from '../../data/repositories/CardRepositoryImpl';
import { GetCardRecommendationsUseCase } from '../../domain/usecases/GetCardRecommendationsUseCase';
import { GetCardListUseCase } from '../../domain/usecases/GetCardListUseCase';

import { LoanRecommendationRemoteDataSource } from '../../data/datasources/LoanRecommendationRemoteDataSource';
import { LoanRecommendationRepositoryImpl } from '../../data/repositories/LoanRecommendationRepositoryImpl';
import { GetLoanRecommendationsUseCase } from '../../domain/usecases/GetLoanRecommendationsUseCase';

import { CreditScoreRemoteDataSource } from '../../data/datasources/CreditScoreRemoteDataSource';
import { CreditScoreRepositoryImpl } from '../../data/repositories/CreditScoreRepositoryImpl';
import { GetCreditScoreUseCase } from '../../domain/usecases/GetCreditScoreUseCase';

// ── User
const userRepo = new UserRepositoryImpl(new UserRemoteDataSource());
const getUserMe = new GetUserMeUseCase(userRepo);
const linkAssets = new LinkAssetsUseCase(userRepo);

// ── Dashboard
const dashboardRepo = new DashboardRepositoryImpl(new DashboardRemoteDataSource());
const getDashboard = new GetDashboardUseCase(dashboardRepo);

// ── Spending
const spendingRepo = new SpendingRepositoryImpl(new SpendingRemoteDataSource());
const getSpending = new GetSpendingUseCase(spendingRepo);

// ── Seedmoney
const seedmoneyRepo = new SeedmoneyRepositoryImpl(new SeedmoneyRemoteDataSource());
const createSeedmoneyAccount = new CreateSeedmoneyAccountUseCase(seedmoneyRepo);
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
const getPassHistory = new GetPassHistoryUseCase(passRepo);

// ── Card
const cardRepo = new CardRepositoryImpl(new CardRemoteDataSource());
const getCardList = new GetCardListUseCase(cardRepo);
const getCardRecommendations = new GetCardRecommendationsUseCase(cardRepo);

// ── Loan
const loanRepo = new LoanRecommendationRepositoryImpl(new LoanRecommendationRemoteDataSource());
const getLoanRecommendations = new GetLoanRecommendationsUseCase(loanRepo);

// ── CreditScore
const creditScoreRepo = new CreditScoreRepositoryImpl(new CreditScoreRemoteDataSource());
const getCreditScore = new GetCreditScoreUseCase(creditScoreRepo);

export const useHomePage = () => {
  const [isAssetLinked, setIsAssetLinked] = useState<boolean | null>(null);
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [spending, setSpending] = useState<Spending | null>(null);
  const [seedMoney, setSeedMoney] = useState<SeedMoneyAccount | null>(null);
  const [allPasses, setAllPasses] = useState<Pass[]>([]);
  const [passSubscriptions, setPassSubscriptions] = useState<PassSubscription[]>([]);
  const [passHistory, setPassHistory] = useState<PassHistoryPage | null>(null);
  const [cardList, setCardList] = useState<CardRecommendation[]>([]);
  const [cardRecommendations, setCardRecommendations] = useState<CardRecommendation[]>([]);
  const [loanRecommendations, setLoanRecommendations] = useState<LoanRecommendationData | null>(null);
  const [creditScore, setCreditScore] = useState<CreditScore | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useDashboardSse(isAssetLinked, setDashboard);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const userMe = await getUserMe.execute();
      setIsAssetLinked(userMe.isAssetLinked);

      if (!userMe.isAssetLinked) return;

      const [
        dashboardResult, spendingResult, accountResult,
        passesResult, subscriptionsResult, passHistoryResult,
        cardListResult, cardsResult, loansResult, creditResult,
      ] = await Promise.allSettled([
        getDashboard.execute(),
        getSpending.execute(),
        getSeedmoneyAccount.execute(),
        getPassProducts.execute(),
        getPassSubscriptions.execute(),
        getPassHistory.execute(0, 10),
        getCardList.execute(),
        getCardRecommendations.execute(),
        getLoanRecommendations.execute(),
        getCreditScore.execute(),
      ]);

      if (dashboardResult.status === 'fulfilled') setDashboard(dashboardResult.value);
      if (spendingResult.status === 'fulfilled') setSpending(spendingResult.value);
      if (accountResult.status === 'fulfilled') setSeedMoney(accountResult.value);
      if (passesResult.status === 'fulfilled') setAllPasses(passesResult.value);
      if (subscriptionsResult.status === 'fulfilled') setPassSubscriptions(subscriptionsResult.value);
      if (passHistoryResult.status === 'fulfilled') setPassHistory(passHistoryResult.value);
      if (cardListResult.status === 'fulfilled') setCardList(cardListResult.value);
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

  const handleLinkAssets = useCallback(async () => {
    await linkAssets.execute();
    setIsAssetLinked(true);
    await fetchAll();
  }, [fetchAll]);

  const createAccount = useCallback(async (accountTypeUniqueNo: string) => {
    const result = await createSeedmoneyAccount.execute(accountTypeUniqueNo);
    setSeedMoney(result);
    return result;
  }, []);

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
    isAssetLinked,
    dashboard,
    spending,
    seedMoney,
    creditScore,
    loanRecommendations,
    cardList,
    cardRecommendations,
    passSubscriptions,
    passHistory,
    allPasses,
    myCards: [] as MyCard[],
    loading,
    error,
    handleLinkAssets,
    createAccount,
    transfer,
    deposit,
    subscribeToPas,
    unsubscribeFromPass,
    saveToPass,
  };
};
