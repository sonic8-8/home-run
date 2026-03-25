import React, { useState } from 'react';
import type { LoanRecommendationData, LoanRecommendation } from '../../../domain/entities/LoanRecommendation';
import styles from './LoanRecommendations.module.css';

interface LoanRecommendationsProps {
  data: LoanRecommendationData | null;
}

type FilterType = '추천대출' | '개인신용대출' | '전세자금대출' | '주택담보대출';

const CHIPS: FilterType[] = ['추천대출', '개인신용대출', '전세자금대출', '주택담보대출'];

const BANK_SHORT: Record<string, string> = {
  '국민은행': 'KB',
  '신한은행': '신한',
  '우리은행': '우리',
  '농협은행주식회사': 'NH',
  '주식회사 하나은행': '하나',
  '주식회사 케이뱅크': 'K뱅크',
  '주식회사 카카오뱅크': '카카오',
  '중소기업은행': 'IBK',
  '수협은행': '수협',
  '부산은행': '부산',
  '광주은행': '광주',
  '전북은행': '전북',
  '경남은행': '경남',
  '제주은행': '제주',
  '한국산업은행': 'KDB',
  '한국스탠다드차타드은행': 'SC',
  '아이엠뱅크': 'IM',
};

function shortBankName(bankName: string): string {
  return BANK_SHORT[bankName] ?? bankName.replace(/주식회사\s*/g, '').replace(/은행.*/, '').slice(0, 4);
}

const CHIP_LABELS: Record<FilterType, string> = {
  '추천대출': '추천',
  '개인신용대출': '신용대출',
  '전세자금대출': '전세대출',
  '주택담보대출': '주담대',
};

export const LoanRecommendations: React.FC<LoanRecommendationsProps> = ({ data }) => {
  const [activeFilter, setActiveFilter] = useState<FilterType>('추천대출');

  const getLoans = (): LoanRecommendation[] => {
    if (!data) return [];
    switch (activeFilter) {
      case '개인신용대출': return data.creditLoans;
      case '전세자금대출': return data.jeonseLoans;
      case '주택담보대출': return data.mortgageLoans;
      default: {
        // 추천: 각 카테고리에서 최저 금리 1개씩
        const picks: LoanRecommendation[] = [];
        if (data.creditLoans.length > 0) picks.push(data.creditLoans[0]);
        if (data.mortgageLoans.length > 0) picks.push(data.mortgageLoans[0]);
        if (data.jeonseLoans.length > 0) picks.push(data.jeonseLoans[0]);
        return picks;
      }
    }
  };

  const loans = getLoans();

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.icon}>🏦</span>
        <div>
          <div className={styles.title}>대출 추천</div>
          <div className={styles.subtitle}>
            {data ? `신용점수 ${data.cssScore}점 (${data.cssGradeLabel}) 기준 맞춤 추천` : '내 신용점수 기준 맞춤 추천'}
          </div>
        </div>
      </div>
      <div className={styles.chips}>
        {CHIPS.map((chip) => (
          <button
            key={chip}
            className={`${styles.chip} ${activeFilter === chip ? styles.chipActive : ''}`}
            onClick={() => setActiveFilter(chip)}
          >
            {CHIP_LABELS[chip]}
          </button>
        ))}
      </div>
      <div className={styles.list}>
        {loans.map((loan, idx) => (
          <React.Fragment key={loan.productId}>
            {idx > 0 && <hr className={styles.divider} />}
            <a
              className={styles.item}
              href={loan.url}
              target="_blank"
              rel="noopener noreferrer"
            >
              <div className={styles.bankBadge}>
                <span className={styles.bankLabel}>{shortBankName(loan.bankName)}</span>
              </div>
              <div className={styles.itemInfo}>
                <div className={styles.itemName}>{loan.productName}</div>
                <div className={styles.itemType}>{loan.productType}</div>
              </div>
              <div className={styles.rateArea}>
                <span className={styles.rateLabel}>예상금리</span>
                <span className={styles.rate}>{loan.estimatedRate.toFixed(2)}%</span>
                <span className={styles.rateSub}>{loan.minRate.toFixed(2)}~{loan.maxRate.toFixed(2)}%</span>
              </div>
            </a>
          </React.Fragment>
        ))}
        {loans.length === 0 && (
          <div className={styles.empty}>
            {data ? '해당 대출 상품이 없습니다.' : '대출 정보를 불러오는 중...'}
          </div>
        )}
      </div>
    </div>
  );
};
