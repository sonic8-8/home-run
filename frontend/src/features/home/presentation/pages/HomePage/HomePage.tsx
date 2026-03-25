import React from 'react';
import { AppHeader } from '../../components/AppHeader/AppHeader';
import { SummaryCards } from '../../components/SummaryCards/SummaryCards';
import { PassWidget } from '../../components/PassWidget/PassWidget';
import { LoanRecommendations } from '../../components/LoanRecommendations/LoanRecommendations';
import { CardRecommendations } from '../../components/CardRecommendations/CardRecommendations';
import { SeedMoneyWidget } from '../../components/SeedMoneyWidget/SeedMoneyWidget';
import { GameBanner } from '../../components/GameBanner/GameBanner';
import { CreditScoreWidget } from '../../components/CreditScoreWidget/CreditScoreWidget';
import { useHomePage } from '../../hooks/useHomePage';
import styles from './HomePage.module.css';

export const HomePage: React.FC = () => {
  const { dashboard, seedMoney, creditScore, loanRecommendations, cardRecommendations, passSubscriptions, allPasses, myCards } =
    useHomePage();

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <div className={styles.inner}>
          <div className={styles.layout}>
            {/* Left column */}
            <div className={styles.leftCol}>
              <SummaryCards dashboard={dashboard} />
              <PassWidget subscriptions={passSubscriptions} allPasses={allPasses} />
              <LoanRecommendations loans={loanRecommendations} />
              <CardRecommendations cards={cardRecommendations} myCards={myCards} />
            </div>
            {/* Right column */}
            <div className={styles.rightCol}>
              {seedMoney && <SeedMoneyWidget account={seedMoney} />}
              <GameBanner />
              <CreditScoreWidget creditScore={creditScore} />
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
