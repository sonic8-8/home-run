import type { ILoanRepository } from '../../domain/repositories/ILoanRepository';
import type { LoanCategory, LoanProduct, LoanProductDetail, LoanProductPage } from '../../domain/entities/LoanProduct';
import type { LoanCalculateParams, LoanCalculation } from '../../domain/entities/LoanCalculation';
import type { LoanApplication, LoanApplicationStatus } from '../../domain/entities/LoanApplication';
import type { LoanConfirmResult, LoanRepayResult, LoanStatus } from '../../domain/entities/ActiveLoan';
import { LoanRemoteDataSource } from '../datasources/LoanRemoteDataSource';

export class LoanRepositoryImpl implements ILoanRepository {
  private readonly dataSource: LoanRemoteDataSource;
  constructor(dataSource: LoanRemoteDataSource) { this.dataSource = dataSource; }

  async getProducts(sessionId: number, category: LoanCategory, page: number, size: number): Promise<LoanProductPage> {
    const m = await this.dataSource.getProducts(sessionId, category, page, size);
    return {
      content: m.content.map((p): LoanProduct => ({
        productId: p.productId,
        bankName: p.bankName,
        bankLogoUrl: p.bankLogoUrl,
        productName: p.productName,
        productType: p.productType as LoanCategory,
        minRate: p.minRate,
        maxRate: p.maxRate,
      })),
      page: m.page,
      size: m.size,
      totalElements: m.totalElements,
      totalPages: m.totalPages,
    };
  }

  async getProductDetail(sessionId: number, productId: string): Promise<LoanProductDetail> {
    const m = await this.dataSource.getProductDetail(sessionId, productId);
    return {
      productId: m.productId,
      bankName: m.bankName,
      bankLogoUrl: m.bankLogoUrl,
      productName: m.productName,
      productType: m.productType as LoanCategory,
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
      status: m.status as LoanApplicationStatus,
      requestInfo: {
        propertyName: m.requestInfo.propertyName,
        propertyPrice: m.requestInfo.propertyPrice,
        applicationDate: m.requestInfo.applicationDate,
      },
      result: {
        maxLoanAmount: m.result.maxLoanAmount,
        rejectionReason: m.result.rejectionReason,
      },
    };
  }

  async confirm(sessionId: number, applicationId: string, requestedAmount: number, agreed: boolean): Promise<LoanConfirmResult> {
    const m = await this.dataSource.confirm(sessionId, { applicationId, requestedAmount, agreed });
    return {
      loanId: m.loanId,
      amount: m.amount,
      annualRate: m.annualRate,
      monthlyPayment: m.monthlyPayment,
      contractDate: m.contractDate,
      status: m.status as LoanStatus,
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
