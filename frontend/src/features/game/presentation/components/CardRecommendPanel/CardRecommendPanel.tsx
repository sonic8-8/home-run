import { useState } from 'react';
import { CardDetailPanel } from './CardDetailPanel';
import type { CardProduct } from './CardDetailPanel';
import styles from './CardRecommendPanel.module.css';

// TODO: GET /games/sessions/{id}/cards/available
const MOCK_CARDS: CardProduct[] = [
  {
    cardId: 1, cardName: '스타벅스 삼성카드', cardImageFrontUrl: '',
    category: 'life', bgColor: '#1a1a2e', icon: '☕',
    annualFee: 15_000, annualBenefit: 30_000,
    summary: '스타벅스 이용 시 사이렌오더 할인의 혜택',
    benefits: [
      '스타벅스 사이렌오더 10% 할인 (월 최대 3,000원)',
      '스타벅스 매장 결제 5% 할인 (월 최대 5,000원)',
      '전월 실적 30만원 이상 시 혜택 제공',
    ],
    rewardConditions: [
      { condition: '전월 실적 50만원 ~ 80만원 미만', detail: '스타벅스 할인 월 3,000원 / 기타 캐시백 월 2,000원' },
      { condition: '전월 실적 80만원 이상', detail: '스타벅스 할인 월 5,000원 / 기타 캐시백 월 5,000원' },
      { condition: '카드 연회비', detail: '50% 할인 (첫 해 7,500원)' },
    ],
  },
  {
    cardId: 2, cardName: '여행 프리미엄 카드', cardImageFrontUrl: '',
    category: 'travel', bgColor: '#f8a5c2', icon: '💗',
    annualFee: 30_000, annualBenefit: 60_000,
    summary: '해외 결제 시 포인트 적립 및 공항 라운지 이용 혜택',
    benefits: [
      '해외 결제 1.5% 포인트 적립',
      '공항 라운지 연 2회 무료',
      '여행자 보험 자동 가입',
    ],
    rewardConditions: [
      { condition: '전월 실적 100만원 이상', detail: '해외 포인트 2배 적립' },
      { condition: '카드 연회비', detail: '30,000원' },
    ],
  },
  {
    cardId: 3, cardName: '주유 특화 카드', cardImageFrontUrl: '',
    category: 'gas', bgColor: '#f0f0f0', icon: '🪙',
    annualFee: 10_000, annualBenefit: 24_000,
    summary: '주유소 결제 시 리터당 최대 100원 할인',
    benefits: [
      'GS칼텍스 / SK에너지 리터당 80원 할인',
      '기타 주유소 리터당 40원 할인',
      '고속도로 통행료 5% 캐시백',
    ],
    rewardConditions: [
      { condition: '전월 실적 50만원 이상', detail: '주유 할인 풀 혜택 제공' },
      { condition: '카드 연회비', detail: '10,000원' },
    ],
  },
  {
    cardId: 4, cardName: '무한 캐시백 카드', cardImageFrontUrl: '',
    category: 'travel', bgColor: '#2d8a7b', icon: '∞',
    annualFee: 20_000, annualBenefit: 48_000,
    summary: '모든 가맹점에서 0.7% 기본 캐시백',
    benefits: [
      '모든 가맹점 0.7% 캐시백',
      '대형마트 1.0% 캐시백',
      '온라인 쇼핑 1.5% 캐시백',
    ],
    rewardConditions: [
      { condition: '전월 실적 30만원 이상', detail: '기본 캐시백 제공' },
      { condition: '카드 연회비', detail: '20,000원' },
    ],
  },
  {
    cardId: 5, cardName: '생활 할인 카드', cardImageFrontUrl: '',
    category: 'life', bgColor: '#d4edda', icon: '🌿',
    annualFee: 0, annualBenefit: 18_000,
    summary: '편의점, 카페, 대중교통 할인 특화 카드',
    benefits: [
      '편의점 10% 할인 (월 최대 2,000원)',
      '카페 5% 할인 (월 최대 1,500원)',
      '대중교통 10% 캐시백 (월 최대 3,000원)',
    ],
    rewardConditions: [
      { condition: '전월 실적 20만원 이상', detail: '할인 혜택 자동 적용' },
      { condition: '카드 연회비', detail: '면제' },
    ],
  },
  {
    cardId: 6, cardName: '전기차 충전 카드', cardImageFrontUrl: '',
    category: 'gas', bgColor: '#fff3cd', icon: '⚡',
    annualFee: 15_000, annualBenefit: 36_000,
    summary: '전기차 충전소 이용 시 kWh당 50원 할인',
    benefits: [
      '전기차 충전 kWh당 50원 할인',
      '주차장 제휴 할인 20%',
      '카 셰어링 서비스 10% 할인',
    ],
    rewardConditions: [
      { condition: '전월 실적 30만원 이상', detail: '충전 할인 혜택 제공' },
      { condition: '카드 연회비', detail: '15,000원' },
    ],
  },
  {
    cardId: 7, cardName: '알라딘 북카드', cardImageFrontUrl: '',
    category: 'recommended', bgColor: '#cce5ff', icon: '📚',
    annualFee: 0, annualBenefit: 12_000,
    summary: '도서 구매 시 최대 10% 적립',
    benefits: [
      '알라딘 10% 포인트 적립',
      '교보문고 / YES24 5% 포인트 적립',
      '문화상품권 교환 가능',
    ],
    rewardConditions: [
      { condition: '전월 실적 10만원 이상', detail: '도서 적립 혜택 자동 적용' },
      { condition: '카드 연회비', detail: '면제' },
    ],
  },
  {
    cardId: 8, cardName: '프리미엄 체크카드', cardImageFrontUrl: '',
    category: 'life', bgColor: '#f8f9fa', icon: '🃏',
    annualFee: 0, annualBenefit: 10_000,
    summary: '체크카드로 누리는 기본 캐시백 혜택',
    benefits: [
      '전 가맹점 0.3% 캐시백',
      '온라인 결제 0.5% 캐시백',
      '연회비 면제',
    ],
    rewardConditions: [
      { condition: '실적 조건 없음', detail: '기본 캐시백 상시 제공' },
      { condition: '카드 연회비', detail: '면제' },
    ],
  },
];

const CARD_ICONS: Record<number, string> = {
  1: '☕', 2: '💗', 3: '🪙', 4: '∞',
  5: '🌿', 6: '⚡', 7: '📚', 8: '🃏',
};

const PAGE_SIZE = 8;

type FilterType = 'all' | 'travel' | 'gas' | 'life' | 'recommended';

const FILTERS: { key: FilterType; label: string }[] = [
  { key: 'all',         label: '전체보기' },
  { key: 'travel',      label: '여행' },
  { key: 'gas',         label: '주유' },
  { key: 'life',        label: '생활' },
  { key: 'recommended', label: '당신을 위한 추천카드' },
];

function filterCards(cards: CardProduct[], filter: FilterType): CardProduct[] {
  if (filter === 'all') return cards;
  return cards.filter((c) => c.category === filter);
}

export function CardRecommendPanel() {
  const [activeFilter, setActiveFilter] = useState<FilterType>('all');
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<CardProduct | null>(null);

  // 상세 화면
  if (selected) {
    return <CardDetailPanel card={selected} onBack={() => setSelected(null)} />;
  }

  const filtered = filterCards(MOCK_CARDS, activeFilter);
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paged = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const handleFilter = (f: FilterType) => {
    setActiveFilter(f);
    setPage(1);
  };

  return (
    <div className={styles.panel}>
      <h2 className={styles.title}>신용카드</h2>

      <div className={styles.filters}>
        {FILTERS.map(({ key, label }) => (
          <button
            key={key}
            className={activeFilter === key ? styles.filterActive : styles.filter}
            onClick={() => handleFilter(key)}
          >
            {label}
          </button>
        ))}
      </div>

      <div className={styles.grid}>
        {paged.map((card) => (
          <div key={card.cardId} className={styles.cardItem} onClick={() => setSelected(card)}>
            <div className={styles.cardImage} style={{ backgroundColor: card.bgColor }}>
              <span className={styles.cardIcon}>{CARD_ICONS[card.cardId]}</span>
            </div>
            <span className={styles.cardName}>{card.cardName}</span>
          </div>
        ))}
      </div>

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button className={styles.pageBtn} onClick={() => setPage(1)} disabled={page === 1}>« First</button>
          <button className={styles.pageBtn} onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={page === 1}>‹ Back</button>
          {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
            <button key={p} className={p === page ? styles.pageBtnActive : styles.pageBtn} onClick={() => setPage(p)}>{p}</button>
          ))}
          <button className={styles.pageBtn} onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>Next ›</button>
          <button className={styles.pageBtn} onClick={() => setPage(totalPages)} disabled={page === totalPages}>Last »</button>
        </div>
      )}
    </div>
  );
}

export default CardRecommendPanel;
