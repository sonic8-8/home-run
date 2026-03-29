import { inject, injectable } from 'tsyringe';
import { ResponseMappingError } from '@core/error/AppError';
import type { ILoanRepository } from '../../domain/repositories/ILoanRepository';
import type { LoanCategory, LoanProduct, LoanProductDetail, LoanProductPage } from '../../domain/entities/LoanProduct';
import type { LoanCalculateParams, LoanCalculation } from '../../domain/entities/LoanCalculation';
import type { LoanApplication, LoanApplicationStatus } from '../../domain/entities/LoanApplication';
import type { LoanConfirmResult, LoanRepayResult, LoanStatus } from '../../domain/entities/ActiveLoan';
import { LoanRemoteDataSource } from '../datasources/LoanRemoteDataSource';

function toLoanCategory(value: string): LoanCategory {
  if (value === 'ALL' || value === 'CREDIT' || value === 'JEONSE' || value === 'MORTGAGE') {
    return value;
  }

  if (value === '개인신용대출') {
    return 'CREDIT';
  }

  if (value === '전세자금대출') {
    return 'JEONSE';
  }

  if (value === '주택담보대출') {
    return 'MORTGAGE';
  }

  throw new ResponseMappingError(`지원하지 않는 대출 상품 유형입니다: ${value}`);
}

function toLoanApplicationStatus(value: string): LoanApplicationStatus {
  if (value === 'APPROVED' || value === 'REJECTED') {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 대출 심사 상태입니다: ${value}`);
}

function toLoanStatus(value: string): LoanStatus {
  if (value === 'ACTIVE' || value === 'OVERDUE' || value === 'CLOSED') {
    return value;
  }

  throw new ResponseMappingError(`지원하지 않는 대출 상태입니다: ${value}`);
}

@injectable()
export class LoanRepositoryImpl implements ILoanRepository {
  private readonly dataSource: LoanRemoteDataSource;
  constructor(
    @inject(LoanRemoteDataSource)
    dataSource: LoanRemoteDataSource,
  ) {
    this.dataSource = dataSource;
  }

  async getProducts(sessionId: number, category: LoanCategory, page: number, size: number): Promise<LoanProductPage> {
    const m = await this.dataSource.getProducts(sessionId, category, page, size);
    const content = m.map((p): LoanProduct => ({
      productId: p.productId,
      bankName: p.bankName,
      bankLogoUrl: p.bankLogoUrl,
      productName: p.productName,
      productType: toLoanCategory(p.productType),
      minRate: p.minRate,
      maxRate: p.maxRate,
    }));

    return {
      content,
      page,
      size,
      totalElements: content.length,
      totalPages: content.length === 0 ? 0 : 1,
    };
  }

  async getProductDetail(sessionId: number, productId: string): Promise<LoanProductDetail> {
    const m = await this.dataSource.getProductDetail(sessionId, productId);
    return {
      productId: m.productId,
      bankName: m.bankName,
      bankLogoUrl: m.bankLogoUrl,
      productName: m.productName,
      productType: toLoanCategory(m.productType),
      minRate: m.minRate,
      maxRate: m.maxRate,
      features: m.features,
    };
  }

  async calculate(sessionId: number, params: LoanCalculateParams): Promise<LoanCalculation> {
    const m = await this.dataSource.calculate(sessionId, {
      repaymentMethod: params.repaymentMethod,
      termMonths: params.termMonths,
      principal: params.principal,
      annualRate: params.annualRate,
    });
    return {
      monthlyPayment: m.monthlyPayment,
      totalInterest: m.totalInterest,
      totalPayment: m.totalPayment,
    };
  }

  async apply(sessionId: number, productId: string, propertyId: string): Promise<LoanApplication> {
    const m = await this.dataSource.apply(sessionId, productId, propertyId);
    return {
      applicationId: m.applicationId,
      status: toLoanApplicationStatus(m.status),
      requestInfo: {
        applicationDate: m.requestInfo.applicationDate,
      },
      result: {
        maxLoanAmount: m.result.maxLoanAmount,
        rejectionReason: m.result.rejectionReason,
      },
    };
  }

  async confirm(sessionId: number, applicationId: number, requestedAmount: number, agreed: boolean): Promise<LoanConfirmResult> {
    const m = await this.dataSource.confirm(sessionId, { applicationId, requestedAmount, agreed });
    return {
      loanId: m.loanId,
      amount: m.amount,
      annualRate: m.annualRate,
      monthlyPayment: m.monthlyPayment,
      contractDate: m.contractDate,
      status: toLoanStatus(m.status),
    };
  }

  async repay(sessionId: number, loanId: number, amount: number): Promise<LoanRepayResult> {
    const m = await this.dataSource.repay(sessionId, { loanId, amount });
    return {
      loanId: m.loanId,
      repaidAmount: m.repaidAmount,
      remainingPrincipal: m.remainingPrincipal,
      updatedMonthlyPayment: m.updatedMonthlyPayment,
    };
  }
}
