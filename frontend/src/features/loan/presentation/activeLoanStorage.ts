import type { LoanConfirmResult } from '@features/loan/domain/entities/ActiveLoan';
import { readSessionStorage, removeSessionStorage, writeSessionStorage } from '@shared/utils/sessionStorage';

export interface StoredActiveLoan {
  readonly loanId: number;
  readonly remainingPrincipal: number;
  readonly annualRate: number;
  readonly monthlyPayment: number;
  readonly contractDate?: string;
  readonly status: 'ACTIVE' | 'OVERDUE' | 'CLOSED';
}

const ACTIVE_LOAN_KEY_PREFIX = 'loan/active';

function getActiveLoanStorageKey(sessionId: number | null): string | null {
  return sessionId === null ? null : `${ACTIVE_LOAN_KEY_PREFIX}/${sessionId}`;
}

export function readActiveLoan(sessionId: number | null): StoredActiveLoan | null {
  const raw = readSessionStorage(getActiveLoanStorageKey(sessionId));
  if (raw === null) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredActiveLoan;
  } catch {
    return null;
  }
}

export function writeActiveLoan(sessionId: number | null, loan: StoredActiveLoan | null): void {
  const key = getActiveLoanStorageKey(sessionId);
  if (loan === null) {
    removeSessionStorage(key);
    return;
  }

  writeSessionStorage(key, JSON.stringify(loan));
}

export function toStoredActiveLoan(loan: LoanConfirmResult): StoredActiveLoan {
  return {
    loanId: loan.loanId,
    remainingPrincipal: loan.amount,
    annualRate: loan.annualRate,
    monthlyPayment: loan.monthlyPayment,
    contractDate: loan.contractDate,
    status: loan.status,
  };
}
