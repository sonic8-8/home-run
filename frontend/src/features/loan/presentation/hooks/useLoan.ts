import { useState, useCallback } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import type { LoanCategory, LoanProductDetail, LoanProductPage } from '../../domain/entities/LoanProduct';
import type { LoanCalculateParams, LoanCalculation } from '../../domain/entities/LoanCalculation';
import type { LoanApplication } from '../../domain/entities/LoanApplication';
import type { LoanConfirmResult, LoanRepayResult } from '../../domain/entities/ActiveLoan';
import { GetLoanProductsUseCase } from '../../domain/usecases/GetLoanProductsUseCase';
import { GetLoanProductDetailUseCase } from '../../domain/usecases/GetLoanProductDetailUseCase';
import { CalculateLoanUseCase } from '../../domain/usecases/CalculateLoanUseCase';
import { ApplyLoanUseCase } from '../../domain/usecases/ApplyLoanUseCase';
import { ConfirmLoanUseCase } from '../../domain/usecases/ConfirmLoanUseCase';
import { RepayLoanUseCase } from '../../domain/usecases/RepayLoanUseCase';

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
        const getProductsUseCase = container.resolve(GetLoanProductsUseCase);
        const result = await getProductsUseCase.execute(sessionId, category, page, size);
        setProductsPage(result);
      } catch (error) {
        setError(toErrorMessage(error));
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
        const getProductDetailUseCase = container.resolve(GetLoanProductDetailUseCase);
        const result = await getProductDetailUseCase.execute(sessionId, productId);
        setSelectedProduct(result);
        return result;
      } catch (error) {
        setError(toErrorMessage(error));
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
        const calculateUseCase = container.resolve(CalculateLoanUseCase);
        const result = await calculateUseCase.execute(sessionId, params);
        setCalculation(result);
        return result;
      } catch (error) {
        setError(toErrorMessage(error));
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
        const applyUseCase = container.resolve(ApplyLoanUseCase);
        const result = await applyUseCase.execute(sessionId, productId, propertyId);
        setApplication(result);
        return result;
      } catch (error) {
        setError(toErrorMessage(error));
        return null;
      } finally {
        setLoading(false);
      }
    },
    [sessionId],
  );

  const confirm = useCallback(
    async (applicationId: number, requestedAmount: number): Promise<LoanConfirmResult | null> => {
      setLoading(true);
      setError(null);
      try {
        const confirmUseCase = container.resolve(ConfirmLoanUseCase);
        const result = await confirmUseCase.execute(sessionId, applicationId, requestedAmount);
        setConfirmedLoan(result);
        return result;
      } catch (error) {
        setError(toErrorMessage(error));
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
        const repayUseCase = container.resolve(RepayLoanUseCase);
        return await repayUseCase.execute(sessionId, loanId, amount);
      } catch (error) {
        setError(toErrorMessage(error));
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
