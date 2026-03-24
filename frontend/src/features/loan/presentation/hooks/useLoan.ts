import { useState, useCallback } from 'react';
import type { LoanCategory, LoanProductDetail, LoanProductPage } from '../../domain/entities/LoanProduct';
import type { LoanCalculateParams, LoanCalculation } from '../../domain/entities/LoanCalculation';
import type { LoanApplication } from '../../domain/entities/LoanApplication';
import type { LoanConfirmResult, LoanRepayResult } from '../../domain/entities/ActiveLoan';
import { LoanRemoteDataSource } from '../../data/datasources/LoanRemoteDataSource';
import { LoanRepositoryImpl } from '../../data/repositories/LoanRepositoryImpl';
import { GetLoanProductsUseCase } from '../../domain/usecases/GetLoanProductsUseCase';
import { GetLoanProductDetailUseCase } from '../../domain/usecases/GetLoanProductDetailUseCase';
import { CalculateLoanUseCase } from '../../domain/usecases/CalculateLoanUseCase';
import { ApplyLoanUseCase } from '../../domain/usecases/ApplyLoanUseCase';
import { ConfirmLoanUseCase } from '../../domain/usecases/ConfirmLoanUseCase';
import { RepayLoanUseCase } from '../../domain/usecases/RepayLoanUseCase';

const dataSource = new LoanRemoteDataSource();
const repository = new LoanRepositoryImpl(dataSource);
const getProductsUseCase = new GetLoanProductsUseCase(repository);
const getProductDetailUseCase = new GetLoanProductDetailUseCase(repository);
const calculateUseCase = new CalculateLoanUseCase(repository);
const applyUseCase = new ApplyLoanUseCase(repository);
const confirmUseCase = new ConfirmLoanUseCase(repository);
const repayUseCase = new RepayLoanUseCase(repository);

export function useLoan(sessionId: number) {
  const [productsPage, setProductsPage] = useState<LoanProductPage | null>(null);
  const [selectedProduct, setSelectedProduct] = useState<LoanProductDetail | null>(null);
  const [calculation, setCalculation] = useState<LoanCalculation | null>(null);
  const [application, setApplication] = useState<LoanApplication | null>(null);
  const [confirmedLoan, setConfirmedLoan] = useState<LoanConfirmResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchProducts = useCallback(
    async (category: LoanCategory = 'ALL', page = 0, size = 10) => {
      setLoading(true);
      setError(null);
      try {
        const result = await getProductsUseCase.execute(sessionId, category, page, size);
        setProductsPage(result);
      } catch {
        setError('대출 상품 목록을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const fetchProductDetail = useCallback(
    async (productId: string): Promise<LoanProductDetail | null> => {
      setLoading(true);
      setError(null);
      try {
        const result = await getProductDetailUseCase.execute(sessionId, productId);
        setSelectedProduct(result);
        return result;
      } catch {
        setError('상품 상세 정보를 불러오지 못했습니다.');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const calculate = useCallback(
    async (params: LoanCalculateParams): Promise<LoanCalculation | null> => {
      setLoading(true);
      setError(null);
      try {
        const result = await calculateUseCase.execute(sessionId, params);
        setCalculation(result);
        return result;
      } catch {
        setError('이자 계산에 실패했습니다.');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const apply = useCallback(
    async (productId: string, propertyId: string): Promise<LoanApplication | null> => {
      setLoading(true);
      setError(null);
      try {
        const result = await applyUseCase.execute(sessionId, productId, propertyId);
        setApplication(result);
        return result;
      } catch {
        setError('대출 심사 신청에 실패했습니다.');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const confirm = useCallback(
    async (applicationId: string, requestedAmount: number): Promise<LoanConfirmResult | null> => {
      setLoading(true);
      setError(null);
      try {
        const result = await confirmUseCase.execute(sessionId, applicationId, requestedAmount);
        setConfirmedLoan(result);
        return result;
      } catch {
        setError('대출 최종 신청에 실패했습니다.');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const repay = useCallback(
    async (loanId: number, amount: number): Promise<LoanRepayResult | null> => {
      setLoading(true);
      setError(null);
      try {
        return await repayUseCase.execute(sessionId, loanId, amount);
      } catch {
        setError('중도 상환에 실패했습니다.');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  return {
    productsPage,
    selectedProduct,
    calculation,
    application,
    confirmedLoan,
    loading,
    error,
    fetchProducts,
    fetchProductDetail,
    calculate,
    apply,
    confirm,
    repay,
  };
}
