import { useCallback, useEffect, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import type { CardProduct } from '../../domain/entities/Card';
import { GetCardListUseCase } from '../../domain/usecases/GetCardListUseCase';
import { GetCardRecommendationsUseCase } from '../../domain/usecases/GetCardRecommendationsUseCase';

export function useCardCatalog() {
  const [recommendedCards, setRecommendedCards] = useState<CardProduct[]>([]);
  const [allCards, setAllCards] = useState<CardProduct[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [recommendationError, setRecommendationError] = useState<string | null>(null);
  const [cardListError, setCardListError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setRecommendationError(null);
    setCardListError(null);

    const getCardRecommendationsUseCase = container.resolve(GetCardRecommendationsUseCase);
    const getCardListUseCase = container.resolve(GetCardListUseCase);

    const [recommendationsResult, cardListResult] = await Promise.allSettled([
      getCardRecommendationsUseCase.execute(),
      getCardListUseCase.execute(),
    ]);

    if (recommendationsResult.status === 'fulfilled') {
      setRecommendedCards(recommendationsResult.value);
    } else {
      setRecommendationError(toErrorMessage(recommendationsResult.reason));
    }

    if (cardListResult.status === 'fulfilled') {
      setAllCards(cardListResult.value);
    } else {
      setCardListError(toErrorMessage(cardListResult.reason));
    }

    setIsLoading(false);
  }, []);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void refresh();
    }, 0);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [refresh]);

  return {
    recommendedCards,
    allCards,
    isLoading,
    recommendationError,
    cardListError,
    refresh,
  };
}
