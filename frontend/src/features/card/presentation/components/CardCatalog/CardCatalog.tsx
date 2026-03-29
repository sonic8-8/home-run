import clsx from 'clsx';
import { useState } from 'react';
import { PageSpinner } from '@shared/components/PageSpinner';
import { AuthImage } from '@shared/components/AuthImage/AuthImage';
import type { CardProduct } from '@features/card/domain/entities/Card';
import { useCardCatalog } from '../../hooks/useCardCatalog';
import { CardDetail } from '../CardDetail';
import styles from './CardCatalog.module.css';

type CardCatalogLayout = 'panel' | 'page';
type CardCatalogTab = 'recommendations' | 'all' | 'mine';

interface CardCatalogProps {
  layout?: CardCatalogLayout;
}

const TAB_LABELS: { key: CardCatalogTab; label: string }[] = [
  { key: 'recommendations', label: '추천 카드' },
  { key: 'all', label: '전체 카드' },
  { key: 'mine', label: '내 카드' },
];

function getTabDescription(tab: CardCatalogTab): string {
  switch (tab) {
    case 'recommendations':
      return '현재 백엔드 추천 API로 내려온 카드 목록입니다.';
    case 'all':
      return '현재 백엔드에 등록된 카드 전체 목록입니다.';
    case 'mine':
      return '보유 카드 조회, 카드 신청, 카드 해지는 이번 티켓 범위에서 지원하지 않습니다.';
  }
}

function getEmptyStateMessage(tab: CardCatalogTab): string {
  return tab === 'recommendations'
    ? '추천 카드가 아직 없습니다.'
    : '조회 가능한 카드가 없습니다.';
}

export function CardCatalog({
  layout = 'panel',
}: CardCatalogProps) {
  const [activeTab, setActiveTab] = useState<CardCatalogTab>('recommendations');
  const [selectedCard, setSelectedCard] = useState<CardProduct | null>(null);
  const {
    recommendedCards,
    allCards,
    isLoading,
    recommendationError,
    cardListError,
    refresh,
  } = useCardCatalog();

  if (selectedCard !== null) {
    return (
      <CardDetail
        card={selectedCard}
        layout={layout}
        onBack={() => setSelectedCard(null)}
      />
    );
  }

  const activeCards = activeTab === 'recommendations'
    ? recommendedCards
    : activeTab === 'all'
      ? allCards
      : [];
  const activeError = activeTab === 'recommendations'
    ? recommendationError
    : activeTab === 'all'
      ? cardListError
      : null;

  return (
    <section
      className={clsx(
        styles.root,
        layout === 'page' ? styles.pageLayout : styles.panelLayout,
      )}
      data-testid="card-catalog"
    >
      <header className={styles.header}>
        <div className={styles.headerBody}>
          <span className={styles.eyebrow}>Read-Only Card Catalog</span>
          <h1 className={styles.title}>카드 추천 조회</h1>
          <p className={styles.description}>
            추천 카드와 전체 카드 목록만 실제 API로 연결했습니다. 신청, 해지, 보유 카드 조회는 준비 중 상태로 노출합니다.
          </p>
        </div>
        <button
          type="button"
          className={styles.refreshButton}
          onClick={() => void refresh()}
          disabled={isLoading}
        >
          다시 조회
        </button>
      </header>

      <nav className={styles.tabs} aria-label="카드 조회 탭">
        {TAB_LABELS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={clsx(
              styles.tab,
              activeTab === tab.key && styles.tabActive,
            )}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <div className={styles.infoBox}>
        <p className={styles.infoText}>{getTabDescription(activeTab)}</p>
      </div>

      {activeTab === 'mine' ? (
        <div className={styles.stateCard} data-testid="card-mine-disabled">
          <strong className={styles.stateTitle}>내 카드 API 준비 중</strong>
          <p className={styles.stateText}>
            현재 백엔드 계약에는 보유 카드 조회, 카드 신청, 카드 해지 API가 없습니다.
          </p>
          <p className={styles.stateText}>
            이번 티켓에서는 추천 카드와 전체 카드 조회만 실제 데이터로 제공합니다.
          </p>
        </div>
      ) : (
        <>
          {activeError && (
            <div className={styles.errorBanner} role="alert">
              <span>{activeError}</span>
              <button
                type="button"
                className={styles.inlineRetryButton}
                onClick={() => void refresh()}
                disabled={isLoading}
              >
                다시 시도
              </button>
            </div>
          )}

          {isLoading && activeCards.length === 0 ? (
            <div className={styles.stateCard} data-testid="card-loading-state">
              <PageSpinner />
            </div>
          ) : activeCards.length === 0 ? (
            <div className={styles.stateCard} data-testid="card-empty-state">
              <strong className={styles.stateTitle}>{getEmptyStateMessage(activeTab)}</strong>
              <p className={styles.stateText}>조회 결과가 생기면 이 영역에 카드가 표시됩니다.</p>
            </div>
          ) : (
            <>
              <div className={styles.summaryRow}>
                <span className={styles.summaryText}>총 {activeCards.length}개의 카드</span>
              </div>
              <div className={styles.grid}>
                {activeCards.map((card) => (
                  <button
                    key={card.id}
                    type="button"
                    className={styles.cardButton}
                    onClick={() => setSelectedCard(card)}
                  >
                    <div className={styles.visualFrame}>
                      <div className={styles.visualFallback}>
                        <span className={styles.visualFallbackText}>{card.issuerName}</span>
                      </div>
                      <AuthImage
                        src={card.imageUrl}
                        alt={card.name}
                        className={styles.visualImage}
                      />
                    </div>
                    <div className={styles.cardBody}>
                      <strong className={styles.cardName}>{card.name}</strong>
                      <span className={styles.cardIssuer}>{card.issuerName}</span>
                      <p className={styles.cardDescription}>{card.description}</p>
                    </div>
                  </button>
                ))}
              </div>
            </>
          )}
        </>
      )}
    </section>
  );
}
