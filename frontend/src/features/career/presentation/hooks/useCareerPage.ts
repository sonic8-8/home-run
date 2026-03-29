import { useCallback, useEffect, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import type { CareerJobType, CareerSession } from '../../domain/entities/CareerSession';
import type { JobOfferList } from '../../domain/entities/JobOffer';
import type { JobTransferResult } from '../../domain/entities/JobTransfer';
import type { SalaryNegotiationResult } from '../../domain/entities/SalaryNegotiation';
import { GetActiveCareerSessionUseCase } from '../../domain/usecases/GetActiveCareerSessionUseCase';
import { GetCareerJobOffersUseCase } from '../../domain/usecases/GetCareerJobOffersUseCase';
import { NegotiateSalaryUseCase } from '../../domain/usecases/NegotiateSalaryUseCase';
import { TransferCareerUseCase } from '../../domain/usecases/TransferCareerUseCase';

interface LoadOffersOptions {
  keepCurrentSalary?: boolean;
}

export function useCareerPage() {
  const [activeSession, setActiveSession] = useState<CareerSession | null>(null);
  const [jobOfferList, setJobOfferList] = useState<JobOfferList | null>(null);
  const [currentSalary, setCurrentSalary] = useState<number | null>(null);
  const [salaryNegotiationResult, setSalaryNegotiationResult] = useState<SalaryNegotiationResult | null>(null);
  const [transferResult, setTransferResult] = useState<JobTransferResult | null>(null);
  const [isSessionLoading, setIsSessionLoading] = useState(true);
  const [isOffersLoading, setIsOffersLoading] = useState(false);
  const [isNegotiating, setIsNegotiating] = useState(false);
  const [transferringOfferId, setTransferringOfferId] = useState<string | null>(null);
  const [pageError, setPageError] = useState<string | null>(null);
  const [offerError, setOfferError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadOffers = useCallback(
    async (sessionId: number, options?: LoadOffersOptions) => {
      setIsOffersLoading(true);
      setOfferError(null);

      try {
        const getCareerJobOffersUseCase = container.resolve(GetCareerJobOffersUseCase);
        const result = await getCareerJobOffersUseCase.execute(sessionId);
        setJobOfferList(result);
        if (options?.keepCurrentSalary !== true) {
          setCurrentSalary(result.offers[0]?.currentSalary ?? null);
        }
      } catch (error) {
        setJobOfferList(null);
        setOfferError(toErrorMessage(error));
      } finally {
        setIsOffersLoading(false);
      }
    },
    [],
  );

  const refresh = useCallback(async () => {
    setIsSessionLoading(true);
    setPageError(null);
    setActionError(null);

    try {
      const getActiveCareerSessionUseCase = container.resolve(GetActiveCareerSessionUseCase);
      const session = await getActiveCareerSessionUseCase.execute();
      setActiveSession(session);
      setTransferResult(null);

      if (session === null) {
        setJobOfferList(null);
        setCurrentSalary(null);
        return;
      }

      await loadOffers(session.sessionId);
    } catch (error) {
      setActiveSession(null);
      setJobOfferList(null);
      setCurrentSalary(null);
      setPageError(toErrorMessage(error));
    } finally {
      setIsSessionLoading(false);
    }
  }, [loadOffers]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const negotiateSalary = useCallback(async () => {
    if (activeSession === null) {
      return;
    }

    setIsNegotiating(true);
    setActionError(null);

    try {
      const negotiateSalaryUseCase = container.resolve(NegotiateSalaryUseCase);
      const result = await negotiateSalaryUseCase.execute(activeSession.sessionId);
      setSalaryNegotiationResult(result);
      setCurrentSalary(result.newSalary);
      await loadOffers(activeSession.sessionId, { keepCurrentSalary: true });
    } catch (error) {
      setActionError(toErrorMessage(error));
    } finally {
      setIsNegotiating(false);
    }
  }, [activeSession, loadOffers]);

  const transferJob = useCallback(async (offerId: string) => {
    if (activeSession === null) {
      return;
    }

    setTransferringOfferId(offerId);
    setActionError(null);

    try {
      const transferCareerUseCase = container.resolve(TransferCareerUseCase);
      const result = await transferCareerUseCase.execute(activeSession.sessionId, offerId);
      setTransferResult(result);
      setCurrentSalary(result.newSalary);
      setActiveSession((previous) => updateSessionJobType(previous, result.newJobType));
      await loadOffers(activeSession.sessionId, { keepCurrentSalary: true });
    } catch (error) {
      setActionError(toErrorMessage(error));
    } finally {
      setTransferringOfferId(null);
    }
  }, [activeSession, loadOffers]);

  return {
    activeSession,
    jobOfferList,
    currentSalary,
    salaryNegotiationResult,
    transferResult,
    isSessionLoading,
    isOffersLoading,
    isNegotiating,
    transferringOfferId,
    pageError,
    offerError,
    actionError,
    refresh,
    negotiateSalary,
    transferJob,
  };
}

function updateSessionJobType(
  session: CareerSession | null,
  newJobType: CareerJobType,
): CareerSession | null {
  if (session === null) {
    return null;
  }

  return {
    ...session,
    jobType: newJobType,
  };
}
