import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { CardCatalog } from './CardCatalog';
import { useCardCatalog } from '../../hooks/useCardCatalog';

vi.mock('../../hooks/useCardCatalog', () => ({
  useCardCatalog: vi.fn(),
}));

vi.mock('@shared/components/PageSpinner', () => ({
  PageSpinner: () => <div>로딩 중</div>,
}));

vi.mock('@shared/components/AuthImage/AuthImage', () => ({
  AuthImage: ({ alt, className }: { alt: string; className?: string }) => (
    <img alt={alt} className={className} />
  ),
}));

const baseCards = [
  {
    id: '1',
    name: '추천 카드 A',
    issuerName: '홈런은행',
    description: '추천 카드 설명',
    baselinePerformanceAmount: 300000,
    maxBenefitLimitAmount: 40000,
    imageUrl: '',
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
];

describe('CardCatalog', () => {
  it('renders recommendation cards from the hook result', () => {
    vi.mocked(useCardCatalog).mockReturnValue({
      recommendedCards: baseCards,
      allCards: [],
      isLoading: false,
      recommendationError: null,
      cardListError: null,
      refresh: vi.fn(),
    });

    render(<CardCatalog />);

    expect(screen.getByText('추천 카드 A')).toBeInTheDocument();
    expect(screen.getAllByText('홈런은행')).toHaveLength(2);
  });

  it('switches to the card list tab and opens the detail view', () => {
    vi.mocked(useCardCatalog).mockReturnValue({
      recommendedCards: [],
      allCards: [
        {
          ...baseCards[0],
          id: '2',
          name: '전체 카드 B',
          issuerName: '국민은행',
        },
      ],
      isLoading: false,
      recommendationError: null,
      cardListError: null,
      refresh: vi.fn(),
    });

    render(<CardCatalog />);

    fireEvent.click(screen.getByRole('button', { name: '전체 카드' }));
    fireEvent.click(screen.getByRole('button', { name: /전체 카드 B/ }));

    expect(screen.getByTestId('card-detail')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '카드 신청 API 준비 중' })).toBeDisabled();
  });

  it('shows the explicit unsupported state for my cards', () => {
    vi.mocked(useCardCatalog).mockReturnValue({
      recommendedCards: [],
      allCards: [],
      isLoading: false,
      recommendationError: null,
      cardListError: null,
      refresh: vi.fn(),
    });

    render(<CardCatalog />);

    fireEvent.click(screen.getByRole('button', { name: '내 카드' }));

    expect(screen.getByTestId('card-mine-disabled')).toBeInTheDocument();
    expect(screen.getByText('내 카드 API 준비 중')).toBeInTheDocument();
  });
});
