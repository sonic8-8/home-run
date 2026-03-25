import type { ILoanRecommendationRepository } from '../../domain/repositories/ILoanRecommendationRepository';
import type { LoanRecommendation, LoanRecommendationData, LoanProductType } from '../../domain/entities/LoanRecommendation';
import type { LoanRecommendationItemModel } from '../models/LoanRecommendationModel';
import { LoanRecommendationRemoteDataSource } from '../datasources/LoanRecommendationRemoteDataSource';

const BANK_URL_MAP: Record<string, string> = {
  '국민은행': 'https://obank.kbstar.com',
  '신한은행': 'https://bank.shinhan.com',
  '농협은행주식회사': 'https://banking.nonghyup.com',
  '우리은행': 'https://spot.wooribank.com',
  '주식회사 하나은행': 'https://www.hanabank.com',
  '주식회사 케이뱅크': 'https://www.kbanknow.com',
  '주식회사 카카오뱅크': 'https://www.kakaobank.com',
  '중소기업은행': 'https://www.ibk.co.kr',
  '수협은행': 'https://www.suhyup-bank.com',
  '부산은행': 'https://www.busanbank.co.kr',
  '광주은행': 'https://www.kjbank.com',
  '전북은행': 'https://www.jbbank.co.kr',
  '경남은행': 'https://www.knbank.co.kr',
  '제주은행': 'https://www.jejubank.co.kr',
  '한국산업은행': 'https://www.kdb.co.kr',
  '한국스탠다드차타드은행': 'https://www.sc.co.kr',
  '아이엠뱅크': 'https://www.imbank.co.kr',
};

function toItem(m: LoanRecommendationItemModel): LoanRecommendation {
  return {
    productId: m.productId,
    bankName: m.bankName,
    bankLogoUrl: '',
    productName: m.productName,
    productType: m.productType as LoanProductType,
    minRate: m.minRate,
    maxRate: m.maxRate,
    estimatedRate: m.estimatedRate,
    url: BANK_URL_MAP[m.bankName] ?? 'https://www.google.com/search?q=' + encodeURIComponent(m.bankName + ' ' + m.productType),
  };
}

export class LoanRecommendationRepositoryImpl implements ILoanRecommendationRepository {
  private readonly dataSource: LoanRecommendationRemoteDataSource;
  constructor(dataSource: LoanRecommendationRemoteDataSource) { this.dataSource = dataSource; }

  async getRecommendations(): Promise<LoanRecommendationData> {
    const m = await this.dataSource.getRecommendations();
    return {
      cssScore: m.cssScore,
      cssGrade: m.cssGrade,
      cssGradeLabel: m.cssGradeLabel,
      estimatedMinRate: m.estimatedMinRate,
      creditLoans: m.creditLoans.map(toItem),
      jeonseLoans: m.jeonseLoans.map(toItem),
      mortgageLoans: m.mortgageLoans.map(toItem),
    };
  }
}
