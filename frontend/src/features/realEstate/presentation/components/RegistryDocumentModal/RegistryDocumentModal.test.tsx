import { fireEvent, render, screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { RegistryDocumentModal } from './RegistryDocumentModal';
import type { ContractResponse, RegistryDocument } from '../../../domain/entities/PropertyDocument';

const baseDocument: RegistryDocument = {
  propertyId: 101,
  propertyName: '테스트 빌라',
  address: '서울시 강남구 테스트로 101',
  salePrice: 350000000,
  documentType: 'REGISTRY_DOCUMENT',
  gapguRows: [
    {
      rankNo: '1',
      purpose: '소유권이전',
      receipt: '2026년 3월 1일',
      reason: '매매',
      details: '홍길동',
    },
  ],
  eulguRows: [
    {
      rankNo: '2',
      purpose: '근저당권설정',
      receipt: '2026년 3월 2일',
      reason: '대출',
      details: '채권최고액 2억원',
    },
  ],
  checklistItems: [
    { trapId: 'OWNER_MISMATCH', label: '소유자 정보가 실제 판매자와 다름' },
    { trapId: 'MORTGAGE_EXISTS', label: '을구에 근저당권이 남아 있음' },
  ],
  solution: {
    verdict: '위험',
    gapgu: {
      verdict: '정상',
      issueSummary: '갑구는 큰 문제가 없습니다.',
      keyPoints: ['소유자 확인'],
      feedbackCorrect: '갑구는 정상입니다.',
      feedbackWrong: '갑구는 소유자만 확인하면 됩니다.',
    },
    eulgu: {
      verdict: '위험',
      issueSummary: '을구에 말소되지 않은 권리가 있습니다.',
      keyPoints: ['근저당권 말소 여부 확인'],
      feedbackCorrect: '을구 위험 요소를 잘 찾았습니다.',
      feedbackWrong: '을구 권리 관계를 다시 확인해야 합니다.',
    },
  },
};

const successfulReview: ContractResponse = {
  success: true,
  trapsDetected: 2,
  trapsCorrectlyIdentified: 2,
  contractResult: 'PARTIAL',
  message: '핵심 위험 요소를 모두 식별했습니다.',
};

describe('RegistryDocumentModal', () => {
  it('enables submit after selecting a checklist item and delegates submit/reset actions', () => {
    const onClose = vi.fn();
    const onToggleTrap = vi.fn();
    const onResetSelection = vi.fn();
    const onSubmitReview = vi.fn();

    const { rerender } = render(
      <RegistryDocumentModal
        isOpen
        onClose={onClose}
        doc={baseDocument}
        selectedTrapIds={[]}
        isSubmitting={false}
        submitError={null}
        reviewResult={null}
        onToggleTrap={onToggleTrap}
        onResetSelection={onResetSelection}
        onSubmitReview={onSubmitReview}
      />,
    );

    expect(screen.getByRole('button', { name: '검토 제출' })).toBeDisabled();

    fireEvent.click(
      within(screen.getByRole('main')).getByLabelText('을구에 근저당권이 남아 있음'),
    );
    expect(onToggleTrap).toHaveBeenCalledWith('MORTGAGE_EXISTS');

    rerender(
      <RegistryDocumentModal
        isOpen
        onClose={onClose}
        doc={baseDocument}
        selectedTrapIds={['MORTGAGE_EXISTS']}
        isSubmitting={false}
        submitError={null}
        reviewResult={null}
        onToggleTrap={onToggleTrap}
        onResetSelection={onResetSelection}
        onSubmitReview={onSubmitReview}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: '선택 초기화' }));
    fireEvent.click(screen.getByRole('button', { name: '검토 제출' }));

    expect(onResetSelection).toHaveBeenCalledTimes(1);
    expect(onSubmitReview).toHaveBeenCalledTimes(1);
  });

  it('shows backend review result and closes with confirm action', () => {
    const onClose = vi.fn();

    render(
      <RegistryDocumentModal
        isOpen
        onClose={onClose}
        doc={baseDocument}
        selectedTrapIds={['OWNER_MISMATCH', 'MORTGAGE_EXISTS']}
        isSubmitting={false}
        submitError={null}
        reviewResult={successfulReview}
        onToggleTrap={vi.fn()}
        onResetSelection={vi.fn()}
        onSubmitReview={vi.fn()}
      />,
    );

    expect(screen.getAllByText('핵심 위험 요소를 모두 식별했습니다.')[0]).toBeInTheDocument();
    expect(screen.getAllByText('2/2')[0]).toBeInTheDocument();
    expect(screen.getAllByText('주의')[0]).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '확인' }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
