import React from 'react';
import { AppHeader } from '../../components/AppHeader/AppHeader';
import { SummaryCards } from '../../components/SummaryCards/SummaryCards';
import { PassWidget } from '../../components/PassWidget/PassWidget';
import { LoanRecommendations } from '../../components/LoanRecommendations/LoanRecommendations';
import { CardRecommendations } from '../../components/CardRecommendations/CardRecommendations';
import { SeedMoneyWidget } from '../../components/SeedMoneyWidget/SeedMoneyWidget';
import { GameBanner } from '../../components/GameBanner/GameBanner';
import { CreditScoreWidget } from '../../components/CreditScoreWidget/CreditScoreWidget';
import { AssetLinkPage } from '../../components/AssetLinkPage/AssetLinkPage';
import { useHomePage } from '../../hooks/useHomePage';
import { readSessionStorage } from '@shared/utils/sessionStorage';
import styles from './HomePage.module.css';

const GAME_SESSION_ID_KEY = 'game:sessionId';

function readActiveGameSessionId(): number | null {
  const raw = readSessionStorage(GAME_SESSION_ID_KEY);

  if (raw === null) {
    return null;
  }

  const parsed = Number.parseInt(raw, 10);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return null;
  }

  return parsed;
}

export const HomePage: React.FC = () => {
  const {
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
    saveToPass,
    unsubscribeFromPass,
    subscribeToPas,
    handleLinkAssets,
    loading,
  } = useHomePage();
  const activeGameSessionId = readActiveGameSessionId();

  if (loading && isAssetLinked === null) return null;

  if (isAssetLinked === false) {
    return <AssetLinkPage onLink={handleLinkAssets} />;
  }

  return (
    <div className={styles.page}>
      <AppHeader />
      <main className={styles.main}>
        <div className={styles.inner}>
          <div className={styles.layout}>
            {/* Left column */}
            <div className={styles.leftCol}>
              <SummaryCards dashboard={dashboard} />
              <PassWidget
                subscriptions={passSubscriptions}
                history={passHistory}
                allPasses={allPasses}
                onSave={saveToPass}
                onUnsubscribe={unsubscribeFromPass}
                onSubscribe={subscribeToPas}
              />
              <LoanRecommendations data={loanRecommendations} />
              <CardRecommendations cards={cardRecommendations} allCards={cardList} />
            </div>
            {/* Right column */}
            <div className={styles.rightCol}>
              {seedMoney && <SeedMoneyWidget account={seedMoney} spending={spending} />}
              <GameBanner
                activeGameSessionId={activeGameSessionId}
              />
              <CreditScoreWidget creditScore={creditScore} />
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
