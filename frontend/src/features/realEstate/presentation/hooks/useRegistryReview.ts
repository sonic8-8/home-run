import { useCallback, useState } from 'react';
import { container } from '@core/di/container';
import { toErrorMessage } from '@core/error/AppError';
import type { ContractResponse, RegistryDocument } from '../../domain/entities/PropertyDocument';
import { GetDocumentsUseCase } from '../../domain/usecases/GetDocumentsUseCase';
import { ContractPropertyUseCase } from '../../domain/usecases/ContractPropertyUseCase';

export function useRegistryReview(sessionId?: number) {
  const [registryDoc, setRegistryDoc] = useState<RegistryDocument | null>(null);
  const [activePropertyId, setActivePropertyId] = useState<string | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoadingDocument, setIsLoadingDocument] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedTrapIds, setSelectedTrapIds] = useState<string[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [reviewResult, setReviewResult] = useState<ContractResponse | null>(null);

  const closeReview = useCallback(() => {
    setRegistryDoc(null);
    setActivePropertyId(null);
    setIsOpen(false);
    setSelectedTrapIds([]);
    setLoadError(null);
    setSubmitError(null);
    setReviewResult(null);
  }, []);

  const openReview = useCallback(async (propertyId: string) => {
    if (sessionId === undefined) {
      return null;
    }

    setIsLoadingDocument(true);
    setLoadError(null);
    setSubmitError(null);
    setReviewResult(null);
    setSelectedTrapIds([]);
    setActivePropertyId(propertyId);

    try {
      const getDocumentsUseCase = container.resolve(GetDocumentsUseCase);
      const document = await getDocumentsUseCase.execute(sessionId, propertyId);
      setRegistryDoc(document);
      setIsOpen(true);
      return document;
    } catch (error) {
      setRegistryDoc(null);
      setActivePropertyId(null);
      setIsOpen(false);
      setLoadError(toErrorMessage(error));
      return null;
    } finally {
      setIsLoadingDocument(false);
    }
  }, [sessionId]);

  const toggleTrap = useCallback((trapId: string) => {
    setSelectedTrapIds((current) => (
      current.includes(trapId)
        ? current.filter((id) => id !== trapId)
        : [...current, trapId]
    ));
  }, []);

  const resetSelection = useCallback(() => {
    if (isSubmitting) {
      return;
    }

    setSelectedTrapIds([]);
    setSubmitError(null);
    setReviewResult(null);
  }, [isSubmitting]);

  const submitReview = useCallback(async () => {
    if (sessionId === undefined || activePropertyId === null) {
      return null;
    }

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const contractPropertyUseCase = container.resolve(ContractPropertyUseCase);
      const result = await contractPropertyUseCase.execute(
        sessionId,
        activePropertyId,
        selectedTrapIds,
      );
      setReviewResult(result);
      return result;
    } catch (error) {
      setSubmitError(toErrorMessage(error));
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [activePropertyId, selectedTrapIds, sessionId]);

  return {
    registryDoc,
    isOpen,
    isLoadingDocument,
    isSubmitting,
    selectedTrapIds,
    loadError,
    submitError,
    reviewResult,
    openReview,
    closeReview,
    toggleTrap,
    resetSelection,
    submitReview,
  };
}
