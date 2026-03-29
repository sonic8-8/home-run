import { describe, expect, it, vi } from 'vitest';
import type { CardRemoteDataSource } from '../datasources/CardRemoteDataSource';
import { CardRepositoryImpl } from './CardRepositoryImpl';

describe('CardRepositoryImpl', () => {
  it('maps the card list response to entities', async () => {
    const dataSource = {
      getCards: vi.fn().mockResolvedValue({
        cards: [
          {
            cardProductId: 1,
            cardName: '홈런 카드',
            cardIssuerName: '홈런은행',
            cardDescription: '생활 할인 카드',
            baselinePerformanceAmount: 300000,
            maxBenefitLimitAmount: 40000,
            cardImageUrl: 'card.png',
            activeBenefits: [
              {
                categoryId: 'life',
                categoryName: '생활',
                categoryDescription: '생활 할인',
                discountRate: 10,
                exampleMerchants: ['스타벅스'],
              },
            ],
          },
        ],
      }),
      getRecommendations: vi.fn(),
    } as Pick<CardRemoteDataSource, 'getCards' | 'getRecommendations'> as CardRemoteDataSource;
    const repository = new CardRepositoryImpl(dataSource);

    await expect(repository.getCards()).resolves.toEqual([
      {
        id: '1',
        name: '홈런 카드',
        issuerName: '홈런은행',
        description: '생활 할인 카드',
        baselinePerformanceAmount: 300000,
        maxBenefitLimitAmount: 40000,
        imageUrl: 'card.png',
        activeBenefits: [
          {
            categoryId: 'life',
            categoryName: '생활',
            categoryDescription: '생활 할인',
            discountRate: 10,
            exampleMerchants: ['스타벅스'],
          },
        ],
      },
    ]);
  });

  it('maps the recommendation response to entities', async () => {
    const dataSource = {
      getCards: vi.fn(),
      getRecommendations: vi.fn().mockResolvedValue({
        recommendations: [
          {
            cardProductId: 7,
            cardName: '추천 카드',
            cardIssuerName: '추천은행',
            cardDescription: '추천 설명',
            baselinePerformanceAmount: null,
            maxBenefitLimitAmount: null,
            cardImageUrl: '',
            activeBenefits: [],
          },
        ],
      }),
    } as Pick<CardRemoteDataSource, 'getCards' | 'getRecommendations'> as CardRemoteDataSource;
    const repository = new CardRepositoryImpl(dataSource);

    await expect(repository.getRecommendations()).resolves.toEqual([
      {
        id: '7',
        name: '추천 카드',
        issuerName: '추천은행',
        description: '추천 설명',
        baselinePerformanceAmount: null,
        maxBenefitLimitAmount: null,
        imageUrl: '',
        activeBenefits: [],
      },
    ]);
  });
});
